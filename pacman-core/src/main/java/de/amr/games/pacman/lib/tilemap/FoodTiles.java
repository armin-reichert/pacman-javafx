/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

public interface FoodTiles {
    byte EMPTY     = 0x00;
    byte PELLET    = 0x01;
    byte ENERGIZER = 0x02;

    static String name(byte value) {
        return switch (value) {
            case EMPTY -> "EMPTY";
            case PELLET -> "PELLET";
            case ENERGIZER -> "ENERGIZER";
            default -> "?";
        };
    }
}
