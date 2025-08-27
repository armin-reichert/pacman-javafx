package de.amr.pacmanfx.tilemap.editor;

import javafx.animation.FadeTransition;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.Instant;

import static de.amr.pacmanfx.tilemap.editor.EditorGlobals.FONT_MESSAGE;

public class MessageManager {

    private Instant messageCloseTime;
    private final Label messageLabel;

    public MessageManager() {
        messageLabel = new Label();
        messageLabel.setFont(FONT_MESSAGE);
        messageLabel.setMinWidth(200);
    }

    public Label messageLabel() {
        return messageLabel;
    }

    public void showMessage(String message, long seconds, MessageType type) {
        Color color = switch (type) {
            case INFO -> Color.BLACK;
            case WARNING -> Color.GREEN;
            case ERROR -> Color.RED;
        };
        messageLabel.setTextFill(color);
        messageLabel.setText(message);
        messageCloseTime = Instant.now().plus(java.time.Duration.ofSeconds(seconds));
    }

    public void clearMessage() {
        showMessage("", 0, MessageType.INFO);
    }

    public void update() {
        if (messageCloseTime != null && Instant.now().isAfter(messageCloseTime)) {
            messageCloseTime = null;
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(2), messageLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                messageLabel.setText("");
                messageLabel.setOpacity(1.0);
            });
            fadeOut.play();
        }
    }




}
