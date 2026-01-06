/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import java.util.stream.Stream;

/**
 * Standard game variants. String value ({@link StandardGameVariant#name()} is uses in map keys etc.
 */
public enum StandardGameVariant {
    ARCADE_PACMAN,
    ARCADE_MS_PACMAN,
    ARCADE_PACMAN_XXL,
    ARCADE_MS_PACMAN_XXL,
    TENGEN_MS_PACMAN;

    public static boolean isArcadeGameName(String variantName) {
        return Stream.of(ARCADE_PACMAN, ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN, ARCADE_MS_PACMAN_XXL)
            .map(StandardGameVariant::name)
            .anyMatch(name -> name.equals(variantName));
    }
}
