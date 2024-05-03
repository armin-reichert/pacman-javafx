package de.amr.games.pacman.model.world;

public interface Tiles {

    byte EMPTY = 0;

    // Terrain
    byte WALL_H    = 1;
    byte WALL_V    = 2;
    byte CORNER_NW = 3;
    byte CORNER_NE = 4;
    byte CORNER_SE = 5;
    byte CORNER_SW = 6;

    byte TUNNEL    = 7;
    byte TERRAIN_END = 8;

    // Food
    byte PELLET = 1;
    byte ENERGIZER = 2;

    byte FOOD_END = 3;
}
