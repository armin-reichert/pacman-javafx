/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.Duration;
import java.time.Instant;

/**
 * Relict from Swing UI implementation, but works fine.
 */
public class FlashMessageView {

    private static class TemporaryMessage {
        private final String text;
        private final double durationSeconds;
        private Instant activationBegin;
        private Instant activationEnd;

        TemporaryMessage(String text, double seconds) {
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

    private static final Font MESSAGE_FONT = Font.font("Sans", FontWeight.BOLD, 30);
    private static final Color TEXT_COLOR_PLAIN = Color.WHEAT;

    private final VBox rootPane = new VBox();

    private final DoubleProperty alpha = new SimpleDoubleProperty(1);
    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>();
    private final ObjectProperty<Color> textFill = new SimpleObjectProperty<>();
    private final ObjectProperty<TemporaryMessage> message = new SimpleObjectProperty<>();

    public FlashMessageView() {
        rootPane.setAlignment(Pos.CENTER);
        rootPane.setMouseTransparent(true);

        final Text textView = new Text();
        textView.setFont(MESSAGE_FONT);
        textView.setTextAlignment(TextAlignment.CENTER);
        textView.fillProperty().bind(textFill);
        textView.textProperty().bind(message.map(msg -> msg != null ? msg.text : ""));

        rootPane.getChildren().add(textView);
        rootPane.backgroundProperty().bind(backgroundColor.map(Background::fill));

        drawTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };
    }

    public Pane rootPane() {
        return rootPane;
    }

    public void start() {
        drawTimer.start();
    }

    public void stop() {
        drawTimer.stop();
    }

    public void clear() {
        message.set(null);
        backgroundColor.set(Color.TRANSPARENT);
    }

    public void showMessage(String messageText, double seconds) {
        if (message.get() != null && message.get().text.equals(messageText)) {
            return;
        }
        message.set(new TemporaryMessage(messageText, seconds));
        message.get().activate();

        rootPane.setVisible(true);
    }

    private void update() {
        if (message.get() == null) {
            rootPane.setVisible(false);
            return;
        }
        if (message.get().hasExpired()) {
            clear();
        } else {
            double activeMillis = Duration.between(message.get().activationBegin, Instant.now()).toMillis();
            alpha.set(Math.cos(0.5 * Math.PI * activeMillis / message.get().activationTimeMillis()));
            Color bgColor = Color.rgb(0, 0, 0, 0.2 + 0.5 * alpha.get());
            backgroundColor.set(bgColor);
            textFill.set(TEXT_COLOR_PLAIN.deriveColor(0, 1, 1, alpha.get()));
        }
    }
}