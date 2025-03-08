/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

public record Waypoint(short x, short y, Direction dir) {

    public Waypoint(Vector2i tile) {
        this((short) tile.x(), (short) tile.y(), null);
    }

    public Waypoint(int x, int y) {
        this((short) x, (short) y, null);
    }

    public Vector2i tile() {
        return new Vector2i(x, y);
    }
}