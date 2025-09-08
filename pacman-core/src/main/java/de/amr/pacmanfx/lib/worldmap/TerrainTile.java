/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.worldmap;

public enum TerrainTile {
    EMPTY          (0x00),
    WALL_H         (0x01),
    WALL_V         (0x02),
    ARC_NW         (0x03),
    ARC_NE         (0x04),
    ARC_SE         (0x05),
    ARC_SW         (0x06),
    TUNNEL         (0x07),
    // 0x08..0x0d
    DOOR           (0x0e),
    // 0x0f
    DARC_NW        (0x10),
    DARC_NE        (0x11),
    DARC_SE        (0x12),
    DARC_SW        (0x13),
    ONE_WAY_UP     (0x14),
    ONE_WAY_RIGHT  (0x15),
    ONE_WAY_DOWN   (0x16),
    ONE_WAY_LEFT   (0x17);

    public final byte $;

    TerrainTile(int code) { $ = (byte) code; }

    public byte code() { return $; }

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
            || code == DARC_NE.$
            || code == DARC_NW.$
            || code == DARC_SE.$
            || code == DARC_SW.$;
    }
}