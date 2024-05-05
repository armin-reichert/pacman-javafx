/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class FadingPane extends BorderPane {

    private final FadeTransition fading;

    public FadingPane() {
        fading = new FadeTransition(Duration.seconds(0.5), this);
        fading.setFromValue(1);
        fading.setToValue(0);
    }

    public void setContent(Node content) {
        setCenter(content);
    }

    /**
     * Makes the pane visible for given duration and then plays the fading animation.
     *
     * @param fadingDelay duration before pane starts fading out
     */
    public void show(Duration fadingDelay) {
        if (fading.getStatus() == Status.RUNNING) {
            return;
        }
        setOpacity(1);
        fading.setDelay(fadingDelay);
        fading.play();
    }
}