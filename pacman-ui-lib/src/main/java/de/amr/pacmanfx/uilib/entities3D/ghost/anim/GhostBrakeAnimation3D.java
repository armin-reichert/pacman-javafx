/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.anim;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.Ghost3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class GhostBrakeAnimation3D extends ManagedAnimation {

    private final Ghost3D ghost3D;

    public GhostBrakeAnimation3D(Ghost3D ghost3D) {
        super("Ghost Braking (%s)".formatted(ghost3D.ghost().name()));
        this.ghost3D = requireNonNull(ghost3D);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        var rotateTransition = new RotateTransition(Duration.seconds(0.5), ghost3D.root());
        rotateTransition.setAxis(Rotate.Y_AXIS);
        rotateTransition.setAutoReverse(true);
        rotateTransition.setCycleCount(2);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);
        return rotateTransition;
    }

    @Override
    public void playFromStart() {
        var rotateTransition = (RotateTransition) animationFX();
        rotateTransition.stop();
        rotateTransition.setByAngle(ghost3D.ghost().worldNavigation().moveDir() == Direction.LEFT ? -35 : 35);
        rotateTransition.playFromStart();
    }

    @Override
    public void playOrContinue() {
        var rotateTransition = (RotateTransition) animationFX();
        rotateTransition.stop();
        rotateTransition.setByAngle(ghost3D.ghost().worldNavigation().moveDir() == Direction.LEFT ? -35 : 35);
        rotateTransition.play();
    }

    @Override
    public void stop() {
        super.stop();
        ghost3D.root().setRotationAxis(Rotate.Y_AXIS);
        ghost3D.root().setRotate(0);
    }
}
