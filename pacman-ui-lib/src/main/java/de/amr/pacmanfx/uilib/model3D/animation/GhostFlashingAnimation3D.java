/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.animation;

import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostComponentColors;
import de.amr.pacmanfx.uilib.model3D.ghost.GhostComponentMaterialSet;
import javafx.animation.*;
import javafx.util.Duration;

import static de.amr.pacmanfx.core.Validations.requireNonNegativeInt;

public class GhostFlashingAnimation3D extends ManagedAnimation {

    private static final float TOTAL_DURATION_SEC = 2;

    private int numFlashes;

    public GhostFlashingAnimation3D(Ghost3D ghost3D) {
        super("Ghost Flashing (%s)".formatted(ghost3D.ghost().name()));
        setFactory(() -> createAnimationFX(ghost3D));
    }

    public int numFlashes() {
        return numFlashes;
    }

    public void setNumFlashes(int numFlashes) {
        requireNonNegativeInt(numFlashes);
        if (this.numFlashes != numFlashes) {
            stop();
            this.numFlashes = numFlashes;
        }
    }

    // Animates the colors of a material set.
    // Repeats cycle (frightenedColor, brightColor, frightenedColor) num flashes times
    private Animation createAnimationFX(Ghost3D ghost3D) {

        if (numFlashes == 0) {
            return new PauseTransition(Duration.seconds(0.5));
        }

        final Duration cycleDuration = Duration.seconds(TOTAL_DURATION_SEC).divide(numFlashes);
        final Duration brightStart = cycleDuration.divide(3);

        final GhostComponentColors brightColors     = ghost3D.config().colors().flashingColors();
        final GhostComponentColors frightenedColors = ghost3D.config().colors().frightenedColors();

        // The set of Phong materials that is used by the ghost 3D during the flashing animation
        final GhostComponentMaterialSet materialSet = ghost3D.materials().flashingMaterial();

        final var flashing = new Timeline(

            new KeyFrame(Duration.ZERO,
                new KeyValue(materialSet.dressMaterial().diffuseColorProperty(),
                    frightenedColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(materialSet.dressMaterial().specularColorProperty(),
                    frightenedColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(materialSet.pupilsMaterial().diffuseColorProperty(),
                    frightenedColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(materialSet.pupilsMaterial().specularColorProperty(),
                    frightenedColors.pupilsColor().brighter(), Interpolator.DISCRETE)
            ),

            new KeyFrame(brightStart,
                new KeyValue(materialSet.dressMaterial().diffuseColorProperty(),
                    brightColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(materialSet.dressMaterial().specularColorProperty(),
                    brightColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(materialSet.pupilsMaterial().diffuseColorProperty(),
                    brightColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(materialSet.pupilsMaterial().specularColorProperty(),
                    brightColors.pupilsColor(), Interpolator.DISCRETE)
            ),

            new KeyFrame(cycleDuration,
                new KeyValue(materialSet.dressMaterial().diffuseColorProperty(),
                    frightenedColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(materialSet.dressMaterial().specularColorProperty(),
                    frightenedColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(materialSet.pupilsMaterial().diffuseColorProperty(),
                    frightenedColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(materialSet.pupilsMaterial().specularColorProperty(),
                    frightenedColors.pupilsColor().brighter(), Interpolator.DISCRETE)
            )
        );

        flashing.setCycleCount(numFlashes);
        return flashing;
    }
}
