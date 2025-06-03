/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.tilemap;

public interface TerrainTiles {
    byte EMPTY               = 0x00;
    byte WALL_H              = 0x01;
    byte WALL_V              = 0x02;
    byte ARC_NW              = 0x03;
    byte ARC_NE              = 0x04;
    byte ARC_SE              = 0x05;
    byte ARC_SW              = 0x06;
    byte TUNNEL              = 0x07;
    byte OBSOLETE_DWALL_H    = 0x08;
    byte OBSOLETE_DWALL_V    = 0x09;
    byte OBSOLETE_DCORNER_NW = 0x0a;
    byte OBSOLETE_DCORNER_NE = 0x0b;
    byte OBSOLETE_DCORNER_SE = 0x0c;
    byte OBSOLETE_DCORNER_SW = 0x0d;
    byte DOOR                = 0x0e;
    byte UNUSED_0f           = 0x0f;
    byte DCORNER_NW          = 0x10;
    byte DCORNER_NE          = 0x11;
    byte DCORNER_SE          = 0x12;
    byte DCORNER_SW          = 0x13;
    byte ONE_WAY_UP          = 0x14;
    byte ONE_WAY_RIGHT       = 0x15;
    byte ONE_WAY_DOWN        = 0x16;
    byte ONE_WAY_LEFT        = 0x17;

    byte MAX_VALUE = 0x17;

    static String name(byte value) {
        return switch (value) {
            case EMPTY         -> "EMPTY";
            case ARC_NW        -> "ARC_NW";
            case ARC_SW        -> "ARC_SW";
            case ARC_SE        -> "ARC_SE";
            case ARC_NE        -> "ARC_NE";
            case DOOR          -> "DOOR";
            case WALL_H        -> "WALL_H";
            case WALL_V        -> "WALL_V";
            case DCORNER_NW    -> "DCORNER_NW";
            case DCORNER_SW    -> "DCORNER_SW";
            case DCORNER_SE    -> "DCORNER_SE";
            case DCORNER_NE    -> "DCORNER_NE";
            case ONE_WAY_UP    -> "ONE_WAY_UP";
            case ONE_WAY_RIGHT -> "ONE_WAY_RIGHT";
            case ONE_WAY_DOWN  -> "ONE_WAY_DOWN";
            case ONE_WAY_LEFT  -> "ONE_WAY_LEFT";
            default            -> "[%d]".formatted(value);
        };
    }
}