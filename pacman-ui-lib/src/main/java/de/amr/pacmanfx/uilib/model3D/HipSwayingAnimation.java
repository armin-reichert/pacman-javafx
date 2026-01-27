/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.RegisteredAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HipSwayingAnimation extends RegisteredAnimation {
    private static final short HIP_ANGLE_FROM = -20;
    private static final short HIP_ANGLE_TO = 20;
    private static final Duration SWING_TIME = Duration.seconds(0.4);
    private static final float POWER_ANGLE_AMPLIFICATION = 1.5f;
    private static final float POWER_RATE = 2;

    private final Node node;

    public HipSwayingAnimation(AnimationRegistry animationRegistry, Node node) {
        super(animationRegistry, "MsPacMan_HipSwaying");
        this.node = requireNonNull(node);
    }

    @Override
    protected RotateTransition createAnimationFX() {
        var rotateTransition = new RotateTransition(SWING_TIME, node);
        rotateTransition.setAxis(Rotate.Z_AXIS);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        return rotateTransition;
    }

    @Override
    public void stop() {
        if (animationFX != null) {
            animationFX.stop();
            var rotateTransition = (RotateTransition) animationFX;
            node.setRotationAxis(rotateTransition.getAxis());
            node.setRotate(0);
        }
    }

    @Override
    public void pause() {
        if (animationFX != null) {
            animationFX.pause();
            var rotateTransition = (RotateTransition) animationFX;
            node.setRotationAxis(rotateTransition.getAxis());
            node.setRotate(0);
        }
    }

    public void setPowerMode(boolean power) {
        if (animationFX != null) {
            boolean wasRunning = animationFX.getStatus() == Animation.Status.RUNNING;
            animationFX.stop();
            animationFX.setRate(power ? POWER_RATE : 1);
            var rotateTransition = (RotateTransition) animationFX;
            double amplification = power ? POWER_ANGLE_AMPLIFICATION : 1;
            rotateTransition.setFromAngle(HIP_ANGLE_FROM * amplification);
            rotateTransition.setToAngle(HIP_ANGLE_TO * amplification);
            if (wasRunning) {
                animationFX.play();
            }
        }
    }

    public void update(Pac pac) {
        if (pac.isParalyzed()) {
            pause();
        } else {
            playOrContinue();
        }
    }
}
