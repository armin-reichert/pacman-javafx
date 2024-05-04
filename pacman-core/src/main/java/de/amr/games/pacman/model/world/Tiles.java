package de.amr.games.pacman.model.world;

public interface Tiles {

    byte A = 10;
    byte B = 11;
    byte C = 12;
    byte D = 13;
    byte E = 14;
    byte F = 15;

    byte EMPTY = 0;

    // Terrain
    byte WALL_H    = 1;
    byte WALL_V    = 2;
    byte CORNER_NW = 3;
    byte CORNER_NE = 4;
    byte CORNER_SE = 5;
    byte CORNER_SW = 6;
    byte TUNNEL    = 7;
    byte DWALL_H   = 8;
    byte DWALL_V   = 9;
    byte DCORNER_NW = A;
    byte DCORNER_NE = B;
    byte DCORNER_SE = C;
    byte DCORNER_SW = D;
    byte DOOR       = E;

    byte TERRAIN_END_MARKER = F;

    static boolean isBlockedTile(byte tile) {
        return tile == WALL_H || tile == WALL_V
            || tile == DWALL_H || tile == DWALL_V
            || tile == CORNER_NE || tile == CORNER_NW || tile == CORNER_SE || tile == CORNER_SW
            || tile == DCORNER_NE || tile == DCORNER_NW || tile == DCORNER_SE || tile == DCORNER_SW;
    }

    // Food
    byte PELLET = 1;
    byte ENERGIZER = 2;
    byte FOOD_END_MARKER = 3;
}
