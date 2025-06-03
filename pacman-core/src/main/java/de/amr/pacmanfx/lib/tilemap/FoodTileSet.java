/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

public class FoodTileSet {

    public enum TileID { EMPTY, PELLET, ENERGIZER; }

    public static byte emptyTileValue() { return valueOf(TileID.EMPTY); }
    public static byte maxTileValue() { return valueOf(TileID.ENERGIZER); }

    public static byte valueOf(TileID tileID) {
        return switch (tileID) {
            case TileID.EMPTY -> 0;
            case TileID.PELLET -> 1;
            case TileID.ENERGIZER -> 2;
        };
    }
}
