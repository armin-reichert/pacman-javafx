/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import de.amr.pacmanfx.lib.math.Vector2i;

public record Vec2Byte(byte x, byte y) {

    public static Vec2Byte vec2Byte(int x, int y) { return new Vec2Byte(x, y); }

    public Vec2Byte(int x, int y) {
        this((byte) x, (byte) y);
    }

    public Vec2Byte(Vector2i tile) {
        this((short) tile.x(), (short) tile.y());
    }

    public Vector2i toVector2i() {
        return new Vector2i(x, y);
    }
}