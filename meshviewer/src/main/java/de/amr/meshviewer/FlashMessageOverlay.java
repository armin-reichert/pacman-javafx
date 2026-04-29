/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.meshviewer;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Create by Copilot AI.
 */
public class FlashMessageOverlay extends StackPane {

    private final Text messageText = new Text();
    private SequentialTransition currentAnimation;

    public FlashMessageOverlay() {
        setMouseTransparent(true);
        setPickOnBounds(false);

        messageText.setFill(Color.WHITE);
        messageText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        messageText.setOpacity(1);

        getChildren().add(messageText);
        setAlignment(messageText, Pos.TOP_CENTER);

        setOpacity(0);
    }

    public void showMessage(String message) {
        messageText.setText(message);

        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), this);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stay = new PauseTransition(Duration.millis(800));

        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), this);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        currentAnimation = new SequentialTransition(fadeIn, stay, fadeOut);
        currentAnimation.play();
    }
}
