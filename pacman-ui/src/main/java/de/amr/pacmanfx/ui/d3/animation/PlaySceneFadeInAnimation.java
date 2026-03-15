/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.ui.d3.PlayScene3D;
import de.amr.pacmanfx.ui.d3.camera.Perspective;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

/**
 * Inner class managing the fade-in animation of the 3D sub-scene.
 * Darkens the background initially and gradually fades to transparent.
 */
public class PlaySceneFadeInAnimation {

    private final Timeline timeline;

    /**
     * Creates a new fade-in animation with the specified duration.
     *
     * @param fadeInDuration duration of the fade from dark to transparent
     * @param playScene3D    the 3D play scene
     */
    public PlaySceneFadeInAnimation(Duration fadeInDuration, PlayScene3D playScene3D) {
        timeline = new Timeline(
            new KeyFrame(Duration.ZERO, _ -> {
                playScene3D.subScene().setFill(PlayScene3D.SCENE_DARK_FILLCOLOR);
                playScene3D.optGameLevel3D().ifPresent(level3D -> level3D.setVisible(true));
                playScene3D.optScores3D().ifPresent(scores3D -> scores3D.setVisible(true));
                // TODO: Verify if startControlling is required here (may be redundant)
                playScene3D.perspectiveManager().currentPerspective().ifPresent(Perspective::startControlling);
            }),
            new KeyFrame(fadeInDuration,
                new KeyValue(playScene3D.subScene().fillProperty(), PlayScene3D.SCENE_BRIGHT_FILLCOLOR, Interpolator.EASE_IN))
        );
    }

    /**
     * Plays the fade-in animation from the beginning.
     */
    public void play() {
        timeline.playFromStart();
    }
}
