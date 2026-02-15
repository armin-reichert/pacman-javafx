/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.animation;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class SwirlAnimation extends RegisteredAnimation {

    private static final float SWIRL_ROTATION_SEC = 1.0f;

    private final Group swirlGroup = new Group();

    public SwirlAnimation(AnimationRegistry animationRegistry, String label) {
        super(animationRegistry, label);
    }

    public Group swirlGroup() {
        return swirlGroup;
    }

    @Override
    protected Animation createAnimationFX() {
        Duration rotationTime = Duration.seconds(SWIRL_ROTATION_SEC);
        var rotation = new RotateTransition(rotationTime, swirlGroup);
        rotation.setAxis(Rotate.Z_AXIS);
        rotation.setFromAngle(0);
        rotation.setToAngle(360);
        rotation.setInterpolator(Interpolator.LINEAR);
        rotation.setCycleCount(Animation.INDEFINITE);
        return rotation;
    }
}
