/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.model3D;

import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public record GhostColoring(
    Color normalDressColor,
    Color normalPupilsColor,
    Color normalEyeballsColor,
    Color frightenedDressColor,
    Color frightenedPupilsColor,
    Color frightenedEyeballsColor,
    Color flashingDressColor,
    Color flashingPupilsColor)
{
    public GhostColoring {
        requireNonNull(normalDressColor);
        requireNonNull(normalPupilsColor);
        requireNonNull(normalEyeballsColor);
        requireNonNull(frightenedDressColor);
        requireNonNull(frightenedPupilsColor);
        requireNonNull(frightenedEyeballsColor);
        requireNonNull(flashingDressColor);
        requireNonNull(flashingPupilsColor);
    }
}
