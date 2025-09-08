/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.mapeditor;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.Instant;

import static de.amr.pacmanfx.mapeditor.EditorGlobals.FONT_MESSAGE;

public class MessageDisplay extends Label {

    private Instant messageCloseTime;

    public MessageDisplay() {
        setFont(FONT_MESSAGE);
        setMinWidth(200);
    }

    public void showMessage(String message, long seconds, MessageType type) {
        Color color = switch (type) {
            case INFO -> Color.BLACK;
            case WARNING -> Color.GREEN;
            case ERROR -> Color.RED;
        };
        setTextFill(color);
        setText(message);
        messageCloseTime = Instant.now().plus(java.time.Duration.ofSeconds(seconds));
    }

    public void clearMessage() {
        showMessage("", 0, MessageType.INFO);
    }

    public void update() {
        if (messageCloseTime != null && Instant.now().isAfter(messageCloseTime)) {
            messageCloseTime = null;
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), this);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                setText("");
                setOpacity(1.0);
            });
            fadeOut.play();
        }
    }
}
