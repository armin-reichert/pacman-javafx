/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.entities3D.ghost.anim;

import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.Ghost3DViewComp;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostComponentColors;
import de.amr.pacmanfx.uilib.entities3D.ghost.comp.GhostSettings;
import javafx.animation.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GhostFlashingAnimation3D extends ManagedAnimation {

    private static final float TOTAL_DURATION_SEC = 2;

    public GhostFlashingAnimation3D(
        Ghost ghost,
        GhostSettings settings,
        int numFlashes
    ) {
        super("Ghost Flashing (%s)".formatted(ghost.name()));
        setAnimationFactory(() -> createAnimationFX(ghost, settings, numFlashes));
    }

    // Animates the colors of a material set.
    // Repeats cycle (frightenedColor, brightColor, frightenedColor) num flashes times
    private Animation createAnimationFX(Ghost ghost, GhostSettings settings, int numFlashes) {

        if (numFlashes == 0) {
            return new PauseTransition(Duration.seconds(0.5));
        }

        final Duration cycleDuration = Duration.seconds(TOTAL_DURATION_SEC).divide(numFlashes);
        final Duration brightStart = cycleDuration.divide(3);

        final GhostComponentColors brightColors     = settings.colors().flashing();
        final GhostComponentColors frightenedColors = settings.colors().frightened();

        final Ghost3DViewComp view3D = ghost.reqComp(Ghost3DViewComp.class);

        final var dressDiffuseColorProperty  = view3D.dressMaterial().diffuseColorProperty();
        final var dressSpecularColorProperty = view3D.dressMaterial().specularColorProperty();

        final var pupilsDiffuseColorProperty  = view3D.pupilsMaterial().diffuseColorProperty();
        final var pupilsSpecularColorProperty = view3D.pupilsMaterial().specularColorProperty();

        final Color dressDiffuseColor = frightenedColors.dressColor();
        final Color dressSpecularColor = dressDiffuseColor.brighter();

        final Color pupilsDiffuseColor = frightenedColors.pupilsColor();
        final Color pupilsSpecularColor = pupilsDiffuseColor.brighter();

        final Color dressFlashingDiffuseColor  = brightColors.dressColor();
        final Color dressFlashingSpecularColor = brightColors.dressColor();

        final Color pupilsFlashingDiffuseColor  = brightColors.pupilsColor();
        final Color pupilsFlashingSpecularColor = brightColors.pupilsColor();

        final var flashing = new Timeline(

            // Set normal colors
            new KeyFrame(Duration.ZERO,
                new KeyValue(dressDiffuseColorProperty,   dressDiffuseColor,     Interpolator.DISCRETE),
                new KeyValue(dressSpecularColorProperty,  dressSpecularColor,    Interpolator.DISCRETE),
                new KeyValue(pupilsDiffuseColorProperty,  pupilsDiffuseColor,    Interpolator.DISCRETE),
                new KeyValue(pupilsSpecularColorProperty, pupilsSpecularColor,   Interpolator.DISCRETE)
            ),

            // Start setting flashing colors
            new KeyFrame(brightStart,
                new KeyValue(dressDiffuseColorProperty,   dressFlashingDiffuseColor,   Interpolator.DISCRETE),
                new KeyValue(dressSpecularColorProperty,  dressFlashingSpecularColor,  Interpolator.DISCRETE),
                new KeyValue(pupilsDiffuseColorProperty,  pupilsFlashingDiffuseColor,  Interpolator.DISCRETE),
                new KeyValue(pupilsSpecularColorProperty, pupilsFlashingSpecularColor, Interpolator.DISCRETE)
            ),

            // Set normal colors
            new KeyFrame(cycleDuration,
                new KeyValue(dressDiffuseColorProperty,   dressDiffuseColor,   Interpolator.DISCRETE),
                new KeyValue(dressSpecularColorProperty,  dressSpecularColor,  Interpolator.DISCRETE),
                new KeyValue(pupilsDiffuseColorProperty,  pupilsDiffuseColor,  Interpolator.DISCRETE),
                new KeyValue(pupilsSpecularColorProperty, pupilsSpecularColor, Interpolator.DISCRETE)
            )
        );

        flashing.setCycleCount(numFlashes);
        return flashing;
    }
}
