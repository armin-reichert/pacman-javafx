/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Standard game variants. String value ({@link GameVariantID#name()} is uses in map keys etc.
 */
public enum GameVariantID {
    ARCADE_PACMAN,
    ARCADE_MS_PACMAN,
    ARCADE_PACMAN_XXL,
    ARCADE_MS_PACMAN_XXL,
    TENGEN_MS_PACMAN;

    public static boolean isArcadeGameName(String variantName) {
        requireNonNull(variantName);
        return Stream.of(ARCADE_PACMAN, ARCADE_PACMAN_XXL, ARCADE_MS_PACMAN, ARCADE_MS_PACMAN_XXL)
            .map(GameVariantID::name)
            .anyMatch(name -> name.equals(variantName));
    }
}
