/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene3d;

import de.amr.games.pacman.lib.EnumMethods;

/**
 * Play scene perspectives.
 *
 * @author Armin Reichert
 */
public enum Perspective implements EnumMethods<Perspective> {
    DRONE, TOTAL, FOLLOWING_PLAYER, NEAR_PLAYER;
}