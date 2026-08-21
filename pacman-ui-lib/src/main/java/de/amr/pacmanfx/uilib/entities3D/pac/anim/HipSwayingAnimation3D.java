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
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HipSwayingAnimation3D extends ManagedAnimation implements Pac3DMovementAnimation {

    private static final short HIP_ANGLE_FROM = -20;
    private static final short HIP_ANGLE_TO = 20;
    private static final Duration SWING_TIME = Duration.seconds(0.4);
    private static final float POWER_ANGLE_AMPLIFICATION = 1.5f;
    private static final float POWER_RATE = 2;

    private final Pac pac;

    public HipSwayingAnimation3D(Pac pac) {
        super("Ms. Pac-Man Hip Swaying");
        this.pac = requireNonNull(pac);
        setAnimationFactory(() -> {
            final Pac3DViewComp view3D = pac.reqComp(Pac3DViewComp.class);
            var rt = new RotateTransition(SWING_TIME, view3D.root());
            rt.setAxis(Rotate.Z_AXIS);
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

        final var rt = (RotateTransition) delegate;
        boolean wasRunning = rt.getStatus() == Animation.Status.RUNNING;

        if (wasRunning) {
            rt.stop();
        }

        if (power) {
            rt.setFromAngle(HIP_ANGLE_FROM * POWER_ANGLE_AMPLIFICATION);
            rt.setToAngle(HIP_ANGLE_TO * POWER_ANGLE_AMPLIFICATION);
            rt.setRate(POWER_RATE);
        }
        else {
            rt.setFromAngle(HIP_ANGLE_FROM);
            rt.setToAngle(HIP_ANGLE_TO);
            rt.setRate(1);
        }

        if (wasRunning) {
            delegate.play();
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
        if (delegate != null) {
            delegate.pause();
            resetPacRotation();
        }
    }

    @Override
    public void update() {
        final PacStateComp state = pac.state();
        final boolean animate = state.enumValue() == PacState.ALIVE && state.isMoving();
        if (animate) {
            playOrContinue();
        } else {
            pause();
        }
    }

    private void resetPacRotation() {
        final Node root = pac.reqComp(Pac3DViewComp.class).root();
        root.setRotationAxis(Rotate.Z_AXIS);
        root.setRotate(0);
    }
}
