/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

import de.amr.pacmanfx.lib.math.Vector2i;

public record Waypoint(short x, short y) {

    public Waypoint(int x, int y) {
        this((short) x, (short) y);
    }

    public static Waypoint wp(int x, int y) { return new Waypoint(x, y); }

    public Waypoint(Vector2i tile) {
        this((short) tile.x(), (short) tile.y());
    }

    public Vector2i tile() {
        return new Vector2i(x, y);
    }
}