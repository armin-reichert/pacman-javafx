/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old.anim;

import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DMaterialSet;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostComponentColors;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import javafx.animation.*;
import javafx.util.Duration;

public class GhostFlashingAnimation3D extends ManagedAnimation {

    private static final float TOTAL_DURATION_SEC = 2;

    public GhostFlashingAnimation3D(
        GhostPersonality gp,
        GhostSettings settings,
        Ghost3DMaterialSet flashingMaterialSet,
        int numFlashes
    ) {
        super("Ghost Flashing (%s)".formatted(gp));
        setFactory(() -> createAnimationFX(settings, flashingMaterialSet, numFlashes));
    }

    // Animates the colors of a material set.
    // Repeats cycle (frightenedColor, brightColor, frightenedColor) num flashes times
    private Animation createAnimationFX(GhostSettings settings, Ghost3DMaterialSet flashingMaterialSet, int numFlashes) {

        if (numFlashes == 0) {
            return new PauseTransition(Duration.seconds(0.5));
        }

        final Duration cycleDuration = Duration.seconds(TOTAL_DURATION_SEC).divide(numFlashes);
        final Duration brightStart = cycleDuration.divide(3);

        final GhostComponentColors brightColors     = settings.colors().flashing();
        final GhostComponentColors frightenedColors = settings.colors().frightened();

        // The set of Phong materials that is used by the ghost 3D during the flashing animation
        //final GhostComponentMaterialSet materialSet = ghost3D.materials().flashingMaterial();

        final var flashing = new Timeline(

            new KeyFrame(Duration.ZERO,
                new KeyValue(flashingMaterialSet.dress().diffuseColorProperty(),
                    frightenedColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.dress().specularColorProperty(),
                    frightenedColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.pupils().diffuseColorProperty(),
                    frightenedColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.pupils().specularColorProperty(),
                    frightenedColors.pupilsColor().brighter(), Interpolator.DISCRETE)
            ),

            new KeyFrame(brightStart,
                new KeyValue(flashingMaterialSet.dress().diffuseColorProperty(),
                    brightColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.dress().specularColorProperty(),
                    brightColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.pupils().diffuseColorProperty(),
                    brightColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.pupils().specularColorProperty(),
                    brightColors.pupilsColor(), Interpolator.DISCRETE)
            ),

            new KeyFrame(cycleDuration,
                new KeyValue(flashingMaterialSet.dress().diffuseColorProperty(),
                    frightenedColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.dress().specularColorProperty(),
                    frightenedColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.pupils().diffuseColorProperty(),
                    frightenedColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(flashingMaterialSet.pupils().specularColorProperty(),
                    frightenedColors.pupilsColor().brighter(), Interpolator.DISCRETE)
            )
        );

        flashing.setCycleCount(numFlashes);
        return flashing;
    }
}
