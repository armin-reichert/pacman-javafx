package de.amr.games.pacman.model.world;

public interface Tiles {

    byte EMPTY = 0;

    // Terrain
    byte WALL      = 1;
    byte TUNNEL    = 2;
    byte CORNER_NW = 3;
    byte CORNER_NE = 4;
    byte CORNER_SE = 5;
    byte CORNER_SW = 6;
    byte TERRAIN_END = 7;

    // Food
    byte PELLET = 1;
    byte ENERGIZER = 2;
    byte FOOD_END = 3;
}
