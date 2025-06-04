/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

public enum FoodTile {

    EMPTY, PELLET, ENERGIZER;

    public static byte emptyTileValue() { return byteValue(EMPTY); }

    public static byte byteValue(FoodTile tile) {
        return switch (tile) {
            case EMPTY -> 0;
            case PELLET -> 1;
            case ENERGIZER -> 2;
        };
    }
}
