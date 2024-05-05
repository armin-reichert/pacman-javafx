/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author Armin Reichert
 */
public class FlashMessageView extends VBox {

    private static class FlashMessage {

        String text;
        double durationSeconds;
        Instant activeFrom;
        Instant activeTo;

        public FlashMessage(String text, double seconds) {
            this.text = text;
            this.durationSeconds = seconds;
        }

        public void activate() {
            activeFrom = Instant.now();
            activeTo = activeFrom.plusMillis(durationMillis());
        }

        public long durationMillis() {
            return Math.round(durationSeconds * 1000);
        }

        public boolean hasExpired() {
            return Instant.now().isAfter(activeTo);
        }
    }

    private final Deque<FlashMessage> activeMessages = new ArrayDeque<>();
    private final Text textView = new Text();
    private Color textColor = Color.WHEAT;
    private Font textFont = Font.font("Sans", FontWeight.BOLD, 30);

    public FlashMessageView() {
        textView.setFont(textFont);
        textView.setTextAlignment(TextAlignment.CENTER);
        setAlignment(Pos.CENTER);
        setMouseTransparent(true);
        getChildren().add(textView);
    }

    public void clear() {
        activeMessages.clear();
    }

    public void showMessage(String messageText, double seconds) {
        if (activeMessages.peek() != null && activeMessages.peek().text.equals(messageText)) {
            activeMessages.poll();
        }
        FlashMessage message = new FlashMessage(messageText, seconds);
        activeMessages.add(message);
        message.activate();
    }

    public void update() {
        if (activeMessages.isEmpty()) {
            setVisible(false);
            return;
        }
        FlashMessage message = activeMessages.peek();
        if (message.hasExpired()) {
            activeMessages.remove();
            return;
        }
        double activeMillis = Duration.between(message.activeFrom, Instant.now()).toMillis();
        double alpha = Math.cos(0.5 * Math.PI * activeMillis / message.durationMillis());
        Color color = Color.rgb(0, 0, 0, 0.2 + 0.5 * alpha);
        setBackground(new Background(new BackgroundFill(color, null, null)));
        textView.setFill(textColor.deriveColor(0, 1, 1, alpha));
        textView.setText(message.text);
        setVisible(true);
    }
}