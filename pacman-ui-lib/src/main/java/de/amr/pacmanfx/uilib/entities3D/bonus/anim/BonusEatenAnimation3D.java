/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.bonus.anim;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class BonusEatenAnimation3D extends ManagedAnimation {

    public BonusEatenAnimation3D(Shape3D bonusShape3D) {
        super("Bonus (Eaten, Points)");
        setFactory(() -> createAnimationFX(bonusShape3D));
    }

    private Animation createAnimationFX(Shape3D bonusShape3D) {
        final var animation = new RotateTransition(Duration.seconds(1), bonusShape3D);
        animation.setAxis(Rotate.X_AXIS);
        animation.setByAngle(360);
        animation.setInterpolator(Interpolator.LINEAR);
        animation.setRate(2);
        animation.setCycleCount(2);
        return animation;
    }
}
