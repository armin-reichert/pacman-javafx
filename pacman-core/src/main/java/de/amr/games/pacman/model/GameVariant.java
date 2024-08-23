/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

/**
 * Game models/variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariant {
    MS_PACMAN, PACMAN, PACMAN_XXL;

    public String resourceKey() {
        return switch (this) {
            case MS_PACMAN  -> "ms_pacman";
            case PACMAN     -> "pacman";
            case PACMAN_XXL -> "pacman_xxl";
        };
    }
}