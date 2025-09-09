/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

public enum FoodTile {
    EMPTY     (0x00),
    PELLET    (0x01),
    ENERGIZER (0x02);

    /** The code for this tile. */
    public final byte $;

    FoodTile(int code) { $ = (byte) code; }
}