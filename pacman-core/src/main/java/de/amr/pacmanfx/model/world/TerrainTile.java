/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.world;

import java.util.stream.Stream;

public enum TerrainTile {
    EMPTY          (0x00),
    WALL_H         (0x01),
    WALL_V         (0x02),

    // Rounded arcs
    ARC_NW         (0x03),
    ARC_NE         (0x04),
    ARC_SE         (0x05),
    ARC_SW         (0x06),

    TUNNEL         (0x07),
    // 0x08..0x0d
    DOOR           (0x0e),
    // 0x0f

    // Angular arcs
    ANG_ARC_NW     (0x10),
    ANG_ARC_NE     (0x11),
    ANG_ARC_SE     (0x12),
    ANG_ARC_SW     (0x13),

    ONE_WAY_UP     (0x14),
    ONE_WAY_RIGHT  (0x15),
    ONE_WAY_DOWN   (0x16),
    ONE_WAY_LEFT   (0x17);

    /** The code for this tile. */
    public final byte $;

    TerrainTile(int code) { $ = (byte) code; }

    public static boolean isValidCode(byte code) {
        return Stream.of(values()).anyMatch(terrainTile -> terrainTile.$ == code);
    }

    /**
     * @param code terrain tile code
     * @return if the tile with this code denotes always blocked terrain (doors are not always blocked!)
     */
    public static boolean isBlocked(byte code) {
        return code == WALL_H.$
            || code == WALL_V.$
            || code == ARC_NE.$
            || code == ARC_NW.$
            || code == ARC_SE.$
            || code == ARC_SW.$
            || code == ANG_ARC_NE.$
            || code == ANG_ARC_NW.$
            || code == ANG_ARC_SE.$
            || code == ANG_ARC_SW.$;
    }

    public static byte mirroredCode(byte code) {
        if (code == TerrainTile.ARC_NE.$)  return TerrainTile.ARC_NW.$;
        if (code == TerrainTile.ARC_NW.$)  return TerrainTile.ARC_NE.$;
        if (code == TerrainTile.ARC_SE.$)  return TerrainTile.ARC_SW.$;
        if (code == TerrainTile.ARC_SW.$)  return TerrainTile.ARC_SE.$;
        if (code == TerrainTile.ANG_ARC_NE.$) return TerrainTile.ANG_ARC_NW.$;
        if (code == TerrainTile.ANG_ARC_NW.$) return TerrainTile.ANG_ARC_NE.$;
        if (code == TerrainTile.ANG_ARC_SE.$) return TerrainTile.ANG_ARC_SW.$;
        if (code == TerrainTile.ANG_ARC_SW.$) return TerrainTile.ANG_ARC_SE.$;
        return code;
    }
}