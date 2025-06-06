/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

public enum TerrainTile {
    EMPTY,
    WALL_H, WALL_V,
    ARC_NW, ARC_NE, ARC_SE, ARC_SW,
    TUNNEL, DOOR,
    DARC_NW, DARC_NE, DARC_SE, DARC_SW, // double stroke arcs
    ONE_WAY_UP, ONE_WAY_RIGHT, ONE_WAY_DOWN, ONE_WAY_LEFT;

    public byte code() {
        return switch (this) {
            case EMPTY         -> 0x00;
            case WALL_H        -> 0x01;
            case WALL_V        -> 0x02;
            case ARC_NW        -> 0x03;
            case ARC_NE        -> 0x04;
            case ARC_SE        -> 0x05;
            case ARC_SW        -> 0x06;
            case TUNNEL        -> 0x07;
            // 0x08..0x0d
            case DOOR          -> 0x0e;
            // 0x0f
            case DARC_NW       -> 0x10;
            case DARC_NE       -> 0x11;
            case DARC_SE       -> 0x12;
            case DARC_SW       -> 0x13;
            case ONE_WAY_UP    -> 0x14;
            case ONE_WAY_RIGHT -> 0x15;
            case ONE_WAY_DOWN  -> 0x16;
            case ONE_WAY_LEFT  -> 0x17;
        };
    }

    /**
     * @param code terrain tile code
     * @return if the tile with this code denotes always blocked terrain (doors are not always blocked!)
     */
    public static boolean isBlocked(byte code) {
        return code == WALL_H.code()
            || code == WALL_V.code()
            || code == ARC_NE.code()
            || code == ARC_NW.code()
            || code == ARC_SE.code()
            || code == ARC_SW.code()
            || code == DARC_NE.code()
            || code == DARC_NW.code()
            || code == DARC_SE.code()
            || code == DARC_SW.code();
    }
}