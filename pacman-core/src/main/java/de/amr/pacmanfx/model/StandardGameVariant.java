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
    MS_PACMAN, MS_PACMAN_TENGEN, MS_PACMAN_XXL, PACMAN, PACMAN_XXL;

    public static boolean isArcadeGameName(String variantName) {
        return Stream.of(PACMAN, PACMAN_XXL, MS_PACMAN, MS_PACMAN_XXL)
            .map(StandardGameVariant::name)
            .anyMatch(name -> name.equals(variantName));
    }
}
