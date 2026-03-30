/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.Bonus3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Bonus3DEatenAnimation extends ManagedAnimation {

    private final Bonus3D bonus3D;

    public Bonus3DEatenAnimation(Bonus3D bonus3D) {
        super("Bonus (Eaten, Points)");
        this.bonus3D = bonus3D;
        setFactory(this::createAnimationFX);
    }

    private Animation createAnimationFX() {
        final var animation = new RotateTransition(Duration.seconds(1), bonus3D);
        animation.setAxis(Rotate.X_AXIS);
        animation.setByAngle(360);
        animation.setInterpolator(Interpolator.LINEAR);
        animation.setRate(2);
        animation.setCycleCount(2);
        return animation;
    }
}
