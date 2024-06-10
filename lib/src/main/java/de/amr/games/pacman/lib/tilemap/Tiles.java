/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

/**
 * @author Armin Reichert
 */
public interface Tiles {

    byte EMPTY = 0;

    // Terrain tiles
    byte WALL_H     = 1;
    byte WALL_V     = 2;
    byte CORNER_NW  = 3;
    byte CORNER_NE  = 4;
    byte CORNER_SE  = 5;
    byte CORNER_SW  = 6;
    byte TUNNEL     = 7;
    byte DWALL_H    = 8;
    byte DWALL_V    = 9;
    byte DCORNER_NW = 10;
    byte DCORNER_NE = 11;
    byte DCORNER_SE = 12;
    byte DCORNER_SW = 13;
    byte DOOR       = 14;

    byte PAC_HOME = 15;

    byte SCATTER_TARGET_RED = 16;
    byte SCATTER_TARGET_PINK = 17;
    byte SCATTER_TARGET_CYAN = 18;
    byte SCATTER_TARGET_ORANGE = 19;

    byte HOME_RED_GHOST = 20;
    byte HOME_PINK_GHOST = 21;
    byte HOME_CYAN_GHOST = 22;
    byte HOME_ORANGE_GHOST = 23;

    byte TERRAIN_TILES_END = 24;

    // Food tiles
    byte PELLET = 1;
    byte ENERGIZER = 2;

    byte FOOD_TILES_END = 3;
}
