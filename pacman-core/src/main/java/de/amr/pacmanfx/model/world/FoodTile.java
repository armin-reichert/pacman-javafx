/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import java.util.stream.Stream;

public enum FoodTile {
    EMPTY     (0x00),
    PELLET    (0x01),
    ENERGIZER (0x02);

    /** The code for this tile. */
    public final byte $;

    FoodTile(int code) { $ = (byte) code; }

    public static boolean isValidCode(byte code) {
        return Stream.of(values()).anyMatch(tile -> tile.$ == code);
    }
}