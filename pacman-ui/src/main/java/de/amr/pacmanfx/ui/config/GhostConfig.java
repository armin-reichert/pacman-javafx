/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.config;

import de.amr.pacmanfx.uilib.model3D.GhostColorSet;
import de.amr.pacmanfx.uilib.model3D.GhostComponentColors;
import javafx.scene.paint.Color;

public record GhostConfig(
    float size2D,
    float size3D,
    Color dressColor,
    Color eyeballColor,
    Color pupilsColor,
    Color frightenedDressColor,
    Color frightenedEyeballColor,
    Color frightenedPupilsColor,
    Color flashinngDressColor,
    Color flashingEyeballColor,
    Color flashingPupilsColor)
{

    /**
     * Creates a color set for a ghost based on its personality.
     *
     * @return the color set for normal, frightened, and flashing states
     */
    public GhostColorSet createGhostColorSet() {
        return new GhostColorSet(
            new GhostComponentColors(
                dressColor(),
                pupilsColor(),
                eyeballColor()
            ),
            new GhostComponentColors(
                frightenedDressColor(),
                frightenedPupilsColor(),
                frightenedEyeballColor()
            ),
            new GhostComponentColors(
                flashinngDressColor(),
                flashingPupilsColor(),
                flashingEyeballColor()
            )
        );
    }
}
