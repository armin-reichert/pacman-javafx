/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._3d;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.AnimationManager;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HeadBangingAnimation extends ManagedAnimation {
    private static final short BANG_ANGLE_FROM = -10;
    private static final short BANG_ANGLE_TO = 15;
    private static final Duration BANG_TIME = Duration.seconds(0.3);
    private static final float POWER_ANGLE_AMPLIFICATION = 2;
    private static final float POWER_RATE = 2;

    private final Node root;

    public HeadBangingAnimation(AnimationManager animationManager, Node root) {
        super(animationManager, "Pac_Man_Movement");
        this.root = requireNonNull(root);
    }

    @Override
    protected Animation createAnimation() {
        var rotateTransition = new RotateTransition(BANG_TIME, root);
        rotateTransition.setAxis(Rotate.X_AXIS);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        return rotateTransition;
    }

    @Override
    public void stop() {
        super.stop();
        var rotateTransition = (RotateTransition) animation;
        root.setRotationAxis(rotateTransition.getAxis());
        root.setRotate(0);
    }

    public void update(Pac pac) {
        var rotateTransition = (RotateTransition) getOrCreateAnimation();
        if (pac.isStandingStill()) {
            pause();
        } else {
            Point3D axis = pac.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (!axis.equals(rotateTransition.getAxis())) {
                stop();
                rotateTransition.setAxis(axis);
            }
            playOrContinue();
        }
    }

    public void setPowerMode(boolean power) {
        var rotateTransition = (RotateTransition) getOrCreateAnimation();
        boolean running = rotateTransition.getStatus() == Animation.Status.RUNNING;
        rotateTransition.stop();
        rotateTransition.setFromAngle(BANG_ANGLE_FROM * POWER_ANGLE_AMPLIFICATION);
        rotateTransition.setToAngle(BANG_ANGLE_TO * POWER_ANGLE_AMPLIFICATION);
        rotateTransition.setRate(power ? POWER_RATE : 1);
        if (running) {
            rotateTransition.play();
        }
    }
}
