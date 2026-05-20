/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.world.NumberBox3D;
import javafx.animation.*;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

/**
 * A rotating box displaying the earned points for eating a ghost,
 * rises to a certain height and fades out.
 */
public class NumberBoxRisingAnimation3D {

    private final NumberBox3D numberBox3D;
    private final double risingHeight;

    public NumberBoxRisingAnimation3D(NumberBox3D numberBox3D, double risingHeight) {
        this.numberBox3D = requireNonNull(numberBox3D);
        this.risingHeight = risingHeight;
    }

    public Animation createAnimation() {
        final Duration riseDuration = Duration.seconds(1);

        // rotate only rotateGroup
        final var rotate = new RotateTransition(Duration.seconds(0.5), numberBox3D.rotateGroup());
        rotate.setAxis(Rotate.X_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(5);
        rotate.setInterpolator(Interpolator.LINEAR);

        // translate only riseGroup (world-space Z)
        final var rise = new TranslateTransition(riseDuration, numberBox3D.riseGroup());
        rise.setFromZ(0);
        rise.setToZ(-risingHeight);
        rise.setInterpolator(Interpolator.EASE_IN);

        return new SequentialTransition(
            Ufx.pauseSec(0.5),
            new ParallelTransition(rotate, rise),
            MaterialColorAnimation3D.fadeOut(1, (PhongMaterial) numberBox3D.box().getMaterial())
        );
    }
}
