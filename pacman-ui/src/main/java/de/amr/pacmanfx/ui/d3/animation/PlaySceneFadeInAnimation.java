/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.ui.d3.PlayScene3D;
import de.amr.pacmanfx.ui.d3.camera.Perspective;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * Inner class managing the fade-in animation of the 3D sub-scene.
 * Darkens the background initially and gradually fades to transparent.
 */
public class PlaySceneFadeInAnimation extends ManagedAnimation {

    /**
     * Creates a new fade-in animation with the specified duration.
     *
     * @param fadeInDuration duration of the fade from dark to transparent
     * @param playScene3D    the 3D play scene
     */
    public PlaySceneFadeInAnimation(Duration fadeInDuration, PlayScene3D playScene3D) {
        super("Play Scene Fade-In");

        requireNonNull(fadeInDuration);
        requireNonNull(playScene3D);

        setFactory(() -> new Timeline(
            new KeyFrame(Duration.ZERO, _ -> {
                // TODO: required?
                playScene3D.perspectiveManager().currentPerspective().ifPresent(Perspective::startControlling);
                playScene3D.optGameLevel3D().ifPresent(level3D -> level3D.setVisible(true));
            }),

            new KeyFrame(Duration.ZERO,
                new KeyValue(playScene3D.subScene().fillProperty(), Color.BLACK),
                new KeyValue(playScene3D.scoreOpacity, 0)
            ),

            new KeyFrame(fadeInDuration,
                new KeyValue(playScene3D.subScene().fillProperty(), Color.TRANSPARENT, Interpolator.EASE_IN),
                new KeyValue(playScene3D.scoreOpacity, 1, Interpolator.EASE_IN)
            )
        ));
    }
}
