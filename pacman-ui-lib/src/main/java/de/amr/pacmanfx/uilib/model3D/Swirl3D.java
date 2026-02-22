/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.lib.Disposable;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Swirl3D extends Group implements Disposable {

    private final ManagedAnimation animation;

    public Swirl3D(AnimationRegistry animationRegistry, String label, float rotationSeconds) {
        animation = new ManagedAnimation(animationRegistry, label);
        animation.setFactory(() -> {
            Duration rotationTime = Duration.seconds(rotationSeconds);
            var rotation = new RotateTransition(rotationTime, this);
            rotation.setAxis(Rotate.Z_AXIS);
            rotation.setFromAngle(0);
            rotation.setToAngle(360);
            rotation.setInterpolator(Interpolator.LINEAR);
            rotation.setCycleCount(Animation.INDEFINITE);
            return rotation;

        });
    }

    public ManagedAnimation animation() {
        return animation;
    }

    @Override
    public void dispose() {
        animation.dispose();
        getChildren().clear();
    }
}
