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
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HeadBangingAnimation extends RegisteredAnimation {
    private static final short BANG_ANGLE_FROM = -10;
    private static final short BANG_ANGLE_TO = 15;
    private static final Duration BANG_TIME = Duration.seconds(0.3);
    private static final float POWER_ANGLE_AMPLIFICATION = 2;
    private static final float POWER_RATE = 2;

    private final Node node;

    public HeadBangingAnimation(AnimationRegistry animationRegistry, Node node) {
        super(animationRegistry, "Pac_Man_Head_Banging");
        this.node = requireNonNull(node);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        var rotateTransition = new RotateTransition(BANG_TIME, node);
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
            node.setRotationAxis(rotateTransition.getAxis());
            node.setRotate(0);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (animationFX != null) {
            var rotateTransition = (RotateTransition) animationFX;
            node.setRotationAxis(rotateTransition.getAxis());
            node.setRotate(0);
        }
    }

    public void update(Pac pac) {
        var rotateTransition = (RotateTransition) getOrCreateAnimationFX();
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
        var rotateTransition = (RotateTransition) getOrCreateAnimationFX();
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
