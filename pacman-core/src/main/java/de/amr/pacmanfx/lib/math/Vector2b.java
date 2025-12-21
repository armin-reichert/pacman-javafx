/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib.math;

public record Vector2b(byte x, byte y) {

    public static Vector2b vec2Byte(int x, int y) { return new Vector2b(x, y); }

    public Vector2b(int x, int y) {
        this((byte) x, (byte) y);
    }

    public Vector2b(Vector2i tile) {
        this((short) tile.x(), (short) tile.y());
    }

    public Vector2i toVector2i() {
        return new Vector2i(x, y);
    }
}