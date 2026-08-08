/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.pac.anim;

import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.entities.pac.comp.PacStateComp;
import de.amr.pacmanfx.core.entities.pac.system.PacStateSystem;
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

    private static final short BANG_ANGLE_FROM = -10;
    private static final short BANG_ANGLE_TO = 15;
    private static final Duration BANG_TIME = Duration.seconds(0.3);
    private static final float POWER_ANGLE_AMPLIFICATION = 2;
    private static final float POWER_RATE = 2;

    private final Node node;

    public HeadBangingAnimation3D(Pac3DViewComp view3D) {
        super("Pac-Man Head Banging");
        requireNonNull(view3D);
        node = view3D.root();
        setAnimationFactory(() -> createAnimationFX(node));
    }

    private Animation createAnimationFX(Node node) {
        var rotateTransition = new RotateTransition(BANG_TIME, node);
        rotateTransition.setAxis(Rotate.X_AXIS);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setInterpolator(Interpolator.EASE_BOTH);
        return rotateTransition;
    }

    @Override
    public ManagedAnimation managedAnimation() {
        return this;
    }

    @Override
    public void stop() {
        super.stop();
        if (delegate != null) {
            var rotateTransition = (RotateTransition) delegate;
            node.setRotationAxis(rotateTransition.getAxis());
            node.setRotate(0);
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (delegate != null) {
            var rotateTransition = (RotateTransition) delegate;
            node.setRotationAxis(rotateTransition.getAxis());
            node.setRotate(0);
        }
    }

    @Override
    public void update(Pac pac, PacStateSystem pacStateSystem) {
        final PacStateComp state = pac.state();
        final WorldNavigationComp worldNavigation = pac.worldNavigation();

        final var rotateTransition = (RotateTransition) delegate();
        final boolean animate = state.pacState() == PacState.ACTIVE && state.isMoving();
        if (animate) {
            final Point3D axis = worldNavigation.moveDir().isVertical() ? Rotate.X_AXIS : Rotate.Y_AXIS;
            if (!axis.equals(rotateTransition.getAxis())) {
                stop();
                rotateTransition.setAxis(axis);
            }
            playOrContinue();
        } else {
            pause();
        }
    }

    @Override
    public void setPowerMode(boolean power) {
        var rotateTransition = (RotateTransition) delegate();
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
