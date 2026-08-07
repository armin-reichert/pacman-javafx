/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost_old.anim;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DMaterialSet;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostComponentColors;
import de.amr.pacmanfx.uilib.entities3D.ghost_old.GhostSettings;
import javafx.animation.*;
import javafx.util.Duration;

public class GhostFlashingAnimation3D extends ManagedAnimation {

    private static final float TOTAL_DURATION_SEC = 2;

    public GhostFlashingAnimation3D(
        Ghost ghost,
        GhostSettings settings,
        Ghost3DMaterialSet flashingMaterialSet,
        int numFlashes) {

        super("Ghost Flashing (%s)".formatted(ghost.name()));
        setFactory(() -> createAnimationFX(ghost, settings, flashingMaterialSet, numFlashes));
    }

    // Animates the colors of a material set.
    // Repeats cycle (frightenedColor, brightColor, frightenedColor) num flashes times
    private Animation createAnimationFX(Ghost ghost, GhostSettings settings, Ghost3DMaterialSet flashingMaterialSet, int numFlashes) {

        if (numFlashes == 0) {
            return new PauseTransition(Duration.seconds(0.5));
        }

        final Duration cycleDuration = Duration.seconds(TOTAL_DURATION_SEC).divide(numFlashes);
        final Duration brightStart = cycleDuration.divide(3);

        final GhostComponentColors brightColors     = settings.colors().flashing();
        final GhostComponentColors frightenedColors = settings.colors().frightened();

        final Ghost3DViewComp view3D = ghost.requireComponent(Ghost3DViewComp.class);

        final var dressDiffuseColorProperty  = view3D.dressMaterial().diffuseColorProperty();
        final var dressSpecularColorProperty = view3D.dressMaterial().specularColorProperty();

        final var pupilsDiffuseColorProperty  = view3D.pupilsMaterial().diffuseColorProperty();
        final var pupilsSpecularColorProperty = view3D.pupilsMaterial().specularColorProperty();

        final var flashing = new Timeline(

            new KeyFrame(Duration.ZERO,
                new KeyValue(dressDiffuseColorProperty,   frightenedColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(dressSpecularColorProperty,  frightenedColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(pupilsDiffuseColorProperty,  frightenedColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(pupilsSpecularColorProperty, frightenedColors.pupilsColor().brighter(), Interpolator.DISCRETE)
            ),

            new KeyFrame(brightStart,
                new KeyValue(dressDiffuseColorProperty,   brightColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(dressSpecularColorProperty,  brightColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(pupilsDiffuseColorProperty,  brightColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(pupilsSpecularColorProperty, brightColors.pupilsColor(), Interpolator.DISCRETE)
            ),

            new KeyFrame(cycleDuration,
                new KeyValue(dressDiffuseColorProperty,   frightenedColors.dressColor(), Interpolator.DISCRETE),
                new KeyValue(dressSpecularColorProperty,  frightenedColors.dressColor().brighter(), Interpolator.DISCRETE),
                new KeyValue(pupilsDiffuseColorProperty,  frightenedColors.pupilsColor(), Interpolator.DISCRETE),
                new KeyValue(pupilsSpecularColorProperty, frightenedColors.pupilsColor().brighter(), Interpolator.DISCRETE)
            )
        );

        flashing.setCycleCount(numFlashes);
        return flashing;
    }
}
