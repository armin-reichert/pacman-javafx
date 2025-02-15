/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

public interface TerrainTiles {

    byte EMPTY = 0;

    // Terrain tiles
    byte WALL_H     = 1;
    byte WALL_V     = 2;

    byte CORNER_NW  = 3;
    byte CORNER_NE  = 4;
    byte CORNER_SE  = 5;
    byte CORNER_SW  = 6;

    byte TUNNEL     = 7;

    byte OBSOLETE_DWALL_H = 8;
    byte OBSOLETE_DWALL_V = 9;

    byte OBSOLETE_DCORNER_NW = 10;
    byte OBSOLETE_DCORNER_NE = 11;
    byte OBSOLETE_DCORNER_SE = 12;
    byte OBSOLETE_DCORNER_SW = 13;

    byte DOOR       = 14;
//    byte WALL       = 15;

    byte DCORNER_ANGULAR_NW = 16;
    byte DCORNER_ANGULAR_NE = 17;
    byte DCORNER_ANGULAR_SE = 18;
    byte DCORNER_ANGULAR_SW = 19;

    byte ONE_WAY_UP    = 20;
    byte ONE_WAY_RIGHT = 21;
    byte ONE_WAY_DOWN  = 22;
    byte ONE_WAY_LEFT  = 23;

    byte LAST_TERRAIN_VALUE = 23; // Adapt when adding new tiles!
}