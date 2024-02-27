/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.EnumMethods;

/**
 * Game variants that can be played.
 *
 * @author Armin Reichert
 */
public enum GameVariant implements EnumMethods<GameVariant> {
    MS_PACMAN, PACMAN;
}