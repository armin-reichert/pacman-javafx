/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3DMovementAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HeadBangingAnimation3D extends Pac3DMovementAnimation {

    private static final short BANG_ANGLE_FROM = -10;
    private static final short BANG_ANGLE_TO = 15;
    private static final Duration BANG_TIME = Duration.seconds(0.3);
    private static final float POWER_ANGLE_AMPLIFICATION = 2;
    private static final float POWER_RATE = 2;

    private final Node targetGroup;

    public HeadBangingAnimation3D(Node targetGroup) {
        super("Pac-Man Head Banging");
        this.targetGroup = requireNonNull(targetGroup);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        var rotateTransition = new RotateTransition(BANG_TIME, targetGroup);
        rotateTransition.setAxis(Rotate.X_AXIS);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        return rotateTransition;
    }

    @Override
    public void stop() {
        super.stop();
        if (animationFX != null) {
            var rotateTransition = (RotateTransition) animationFX;
            targetGroup.setRotationAxis(rotateTransition.getAxis());
            targetGroup.setRotate(0);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (animationFX != null) {
            var rotateTransition = (RotateTransition) animationFX;
            targetGroup.setRotationAxis(rotateTransition.getAxis());
            targetGroup.setRotate(0);
        }
    }

    public void update(Pac pac) {
        var rotateTransition = (RotateTransition) animationFX();
        if (pac.isParalyzed()) {
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
        var rotateTransition = (RotateTransition) animationFX();
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
