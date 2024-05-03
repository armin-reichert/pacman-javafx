package de.amr.games.pacman.model.world;

public interface Tiles {

    byte EMPTY = 0;

    // Terrain
    byte WALL = 1;
    byte TUNNEL = 2;

    // Food
    byte PELLET = 1;
    byte ENERGIZER = 2;
}
