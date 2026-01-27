/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.Duration;
import java.time.Instant;

//TODO use JavaFX features for fading etc.
public class FlashMessageView extends VBox {

    private static class Message {
        private final String text;
        private final double durationSeconds;
        private Instant activationBegin;
        private Instant activationEnd;

        Message(String text, double seconds) {
            this.text = text;
            this.durationSeconds = seconds;
        }

        void activate() {
            activationBegin = Instant.now();
            activationEnd = activationBegin.plusMillis(activationTimeMillis());
        }

        long activationTimeMillis() {
            return Math.round(durationSeconds * 1000);
        }

        boolean hasExpired() {
            return Instant.now().isAfter(activationEnd);
        }
    }

    private final AnimationTimer drawTimer;

    private  static final Font MESSAGE_FONT = Font.font("Sans", FontWeight.BOLD, 30);

    private final Text textView = new Text();
    private final Color textColor = Color.WHEAT;
    private Message message;

    public FlashMessageView() {
        textView.setFont(MESSAGE_FONT);
        textView.setTextAlignment(TextAlignment.CENTER);
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);
        getChildren().add(textView);
        drawTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
    }

    public void start() {
        drawTimer.start();
    }

    public void stop() {
        drawTimer.stop();
    }

    public void clear() {
        textView.setText(null);
        message = null;
        setBackground(Background.fill(Color.TRANSPARENT));
    }

    public void showMessage(String messageText, double seconds) {
        if (message != null && message.text.equals(messageText)) {
            return;
        }
        message = new Message(messageText, seconds);
        message.activate();
        setVisible(true);
    }

    private void update() {
        if (message == null) {
            setVisible(false);
            return;
        }
        if (message.hasExpired()) {
            clear();
        } else {
            double activeMillis = Duration.between(message.activationBegin, Instant.now()).toMillis();
            double alpha = Math.cos(0.5 * Math.PI * activeMillis / message.activationTimeMillis());
            Color backgroundColor = Color.rgb(0, 0, 0, 0.2 + 0.5 * alpha);
            setBackground(Background.fill(backgroundColor));
            textView.setFill(textColor.deriveColor(0, 1, 1, alpha));
            textView.setText(message.text);
        }
    }
}