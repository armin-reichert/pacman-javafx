/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HipSwayingAnimation extends ManagedAnimation {
    private static final short HIP_ANGLE_FROM = -20;
    private static final short HIP_ANGLE_TO = 20;
    private static final Duration SWING_TIME = Duration.seconds(0.4);
    private static final float POWER_ANGLE_AMPLIFICATION = 1.5f;
    private static final float POWER_RATE = 2;

    private final Node node;

    public HipSwayingAnimation(AnimationManager animationManager, Node node) {
        super(animationManager, "MsPacMan_HipSwaying");
        this.node = requireNonNull(node);
    }

    @Override
    protected Animation createAnimation() {
        var rotateTransition = new RotateTransition(SWING_TIME, node);
        rotateTransition.setAxis(Rotate.Z_AXIS);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        return rotateTransition;
    }

    @Override
    public void stop() {
        var rotateTransition = (RotateTransition) getOrCreateAnimation();
        super.stop();
        node.setRotationAxis(rotateTransition.getAxis());
        node.setRotate(0);
    }

    public void setPowerMode(boolean power) {
        var rotateTransition = (RotateTransition) getOrCreateAnimation();
        boolean running = rotateTransition.getStatus() == Animation.Status.RUNNING;
        double amplification = power ? POWER_ANGLE_AMPLIFICATION : 1;
        rotateTransition.stop();
        rotateTransition.setFromAngle(HIP_ANGLE_FROM * amplification);
        rotateTransition.setToAngle(HIP_ANGLE_TO * amplification);
        rotateTransition.setRate(power ? POWER_RATE : 1);
        if (running) {
            rotateTransition.play();
        }
    }
}
