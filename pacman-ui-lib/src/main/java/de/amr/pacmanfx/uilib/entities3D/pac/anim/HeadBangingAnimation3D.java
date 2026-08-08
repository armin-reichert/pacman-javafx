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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class HeadBangingAnimation3D extends ManagedAnimation implements Pac3DMovementAnimation {

    private static final short    BANG_ANGLE_FROM = -10;
    private static final short    BANG_ANGLE_TO = 15;

    private static final Duration BANG_TIME = Duration.seconds(0.3);

    private static final float    POWER_ANGLE_AMPLIFICATION = 2.0f;
    private static final float    POWER_RATE = 2.0f;

    private final Pac pac;

    private final DoubleProperty fromAngle = new SimpleDoubleProperty(BANG_ANGLE_FROM);
    private final DoubleProperty toAngle   = new SimpleDoubleProperty(BANG_ANGLE_TO);
    private final DoubleProperty rate      = new SimpleDoubleProperty(1);

    public HeadBangingAnimation3D(Pac pac) {
        super("Pac-Man Head Banging");
        this.pac = requireNonNull(pac);

        setAnimationFactory(() -> {
            final Pac3DViewComp view3D = pac.requireComponent(Pac3DViewComp.class);
            // Warning: RT is banned in fascist EU!
            var rt = new RotateTransition(BANG_TIME, view3D.root());
            rt.fromAngleProperty().bind(fromAngle);
            rt.toAngleProperty().bind(toAngle);
            rt.rateProperty().bind(rate);
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

        if (power) {
            fromAngle.set(BANG_ANGLE_FROM * POWER_ANGLE_AMPLIFICATION);
            toAngle.set(BANG_ANGLE_TO * POWER_ANGLE_AMPLIFICATION);
            rate.set(POWER_RATE);
        } else {
            fromAngle.set(BANG_ANGLE_FROM);
            toAngle.set(BANG_ANGLE_TO);
            rate.set(1);
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
    public void update() {
        final PacStateComp state = pac.state();
        final boolean animate = state.enumValue() == PacState.ACTIVE && state.isMoving();
        if (animate) {
            playOrContinue();
        } else {
            pause();
        }
    }
}
