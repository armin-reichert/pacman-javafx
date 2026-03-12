/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.MutableGhost3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class Ghost3DPointsAnimation extends ManagedAnimation {

    private final MutableGhost3D mutableGhost3D;

    public Ghost3DPointsAnimation(AnimationRegistry animationRegistry, MutableGhost3D mutableGhost3D) {
        super(animationRegistry, "Ghost_Points_%s".formatted(mutableGhost3D.ghost().name()));
        this.mutableGhost3D = requireNonNull(mutableGhost3D);
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        var numberBoxRotation = new RotateTransition(Duration.seconds(1), mutableGhost3D.numberShape3D());
        numberBoxRotation.setAxis(Rotate.X_AXIS);
        numberBoxRotation.setFromAngle(0);
        numberBoxRotation.setToAngle(360);
        numberBoxRotation.setInterpolator(Interpolator.LINEAR);
        numberBoxRotation.setRate(0.75);
        return numberBoxRotation;
    }
}
