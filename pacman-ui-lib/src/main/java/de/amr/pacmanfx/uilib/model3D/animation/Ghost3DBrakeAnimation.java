/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class Ghost3DBrakeAnimation extends ManagedAnimation {

    private final MutableGhost3D mutableGhost3D;

    public Ghost3DBrakeAnimation(AnimationRegistry animationRegistry, MutableGhost3D mutableGhost3D) {
        super(animationRegistry, "Ghost_Braking_%s".formatted(mutableGhost3D.ghost().name()));
        this.mutableGhost3D = requireNonNull(mutableGhost3D);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        var rotateTransition = new RotateTransition(Duration.seconds(0.5), mutableGhost3D);
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
        rotateTransition.setByAngle(mutableGhost3D.ghost().moveDir() == Direction.LEFT ? -35 : 35);
        rotateTransition.playFromStart();
    }

    @Override
    public void playOrContinue() {
        var rotateTransition = (RotateTransition) animationFX();
        rotateTransition.stop();
        rotateTransition.setByAngle(mutableGhost3D.ghost().moveDir() == Direction.LEFT ? -35 : 35);
        rotateTransition.play();
    }

    @Override
    public void stop() {
        super.stop();
        mutableGhost3D.setRotationAxis(Rotate.Y_AXIS);
        mutableGhost3D.setRotate(0);
    }
}
