/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.view3d.animation;

import de.amr.basics.ui.animation.ManagedAnimation;
import de.amr.pacmanfx.ui.gamescene.view3d.PlaySceneView3D;
import de.amr.pacmanfx.ui.gamescene.view3d.camera.Perspective;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class PlaySceneView3D_FadeInAnimation extends ManagedAnimation {

    public static final String ANIMATION_NAME = "PlayScene Fade-In Animation";

    public PlaySceneView3D_FadeInAnimation(Duration fadeInDuration, PlaySceneView3D view3D) {
        super(ANIMATION_NAME);

        requireNonNull(fadeInDuration);
        requireNonNull(view3D);

        setAnimationFactory(() -> new Timeline(
            new KeyFrame(Duration.ZERO,
                _ -> view3D.perspectiveManager().currentPerspective().ifPresent(Perspective::startControlling)),

            new KeyFrame(Duration.ZERO,
                new KeyValue(view3D.subScene().fillProperty(), Color.BLACK),
                new KeyValue(view3D.opacityProperty(), 0)
            ),

            new KeyFrame(fadeInDuration,
                new KeyValue(view3D.subScene().fillProperty(), Color.TRANSPARENT, Interpolator.EASE_IN),
                new KeyValue(view3D.opacityProperty(), 1, Interpolator.EASE_IN)
            )
        ));
    }
}
