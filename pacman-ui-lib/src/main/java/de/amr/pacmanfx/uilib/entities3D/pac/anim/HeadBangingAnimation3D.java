/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.anim;

import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.pac.comp.Pac3DViewComp;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HeadBangingAnimation3D extends ManagedAnimation implements Pac3DMovementAnimation {

    private static final short    BANG_ANGLE_FROM = -10;
    private static final short    BANG_ANGLE_TO = 15;
    private static final Duration BANG_TIME = Duration.seconds(0.3);
    private static final float    POWER_ANGLE_AMPLIFICATION = 2.0f;
    private static final float    POWER_RATE = 2.0f;

    private final Pac pac;

    public HeadBangingAnimation3D(Pac pac) {
        super("Pac-Man Head Banging");
        this.pac = requireNonNull(pac);
        setAnimationFactory(() -> {
            final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
            // Warning: RT is banned in fascist EU!
            var rt = new RotateTransition(BANG_TIME, view3D.root());
            rt.setAxis(computeAxis());
            rt.setFromAngle(BANG_ANGLE_FROM);
            rt.setToAngle(BANG_ANGLE_TO);
            rt.setCycleCount(Animation.INDEFINITE);
            rt.setAutoReverse(true);
            rt.setInterpolator(Interpolator.EASE_BOTH);
            return rt;
        });
    }

    @Override
    public void setPowerMode(boolean power) {
        if (delegate == null) {
            return;
        }

        final var rt = (RotateTransition) delegate();
        final boolean wasRunning = rt.getStatus() == Animation.Status.RUNNING;

        if (wasRunning) {
            rt.stop();
        }

        rt.setAxis(computeAxis());
        if (power) {
            rt.setFromAngle(BANG_ANGLE_FROM * POWER_ANGLE_AMPLIFICATION);
            rt.setToAngle(BANG_ANGLE_TO * POWER_ANGLE_AMPLIFICATION);
            rt.setRate(POWER_RATE);
        } else {
            rt.setFromAngle(BANG_ANGLE_FROM);
            rt.setToAngle(BANG_ANGLE_TO);
            rt.setRate(1);
        }

        if (wasRunning) {
            rt.play();
        }
    }

    @Override
    public ManagedAnimation managedAnimation() {
        return this;
    }

    @Override
    public void stop() {
        super.stop();
        resetPacRotation();
    }

    @Override
    public void pause() {
        super.pause();
        resetPacRotation();
    }

    @Override
    public void update() {
        final PacStateComp state = pac.state();
        final boolean animate = state.enumValue() == PacState.ACTIVE && state.isMoving();
        if (animate) {
            final Point3D axis = computeAxis();
            final var rt = (RotateTransition) delegate();
            if (!axis.equals(rt.getAxis())) {
                stop();
                rt.setAxis(axis);
            }
            playOrContinue();
        } else {
            pause();
        }
    }

    private void resetPacRotation() {
        final Node root = pac.requireComponent(Pac3DViewComp.class).root();
        root.setRotationAxis(computeAxis());
        root.setRotate(0);
    }

    private Point3D computeAxis() {
        return pac.worldNavigation().moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
    }
}
