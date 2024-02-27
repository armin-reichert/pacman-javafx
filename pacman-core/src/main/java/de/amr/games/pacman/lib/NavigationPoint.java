/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.util.Objects;

/**
 * @author Armin Reichert
 */
public final class NavigationPoint {

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NavigationPoint [x=");
        builder.append(x);
        builder.append(", y=");
        builder.append(y);
        builder.append(", dir=");
        builder.append(dir);
        builder.append("]");
        return builder.toString();
    }

    public static NavigationPoint np(Vector2i tile, Direction dir) {
        return new NavigationPoint(tile.x(), tile.y(), dir);
    }

    public static NavigationPoint np(Vector2i tile) {
        return new NavigationPoint(tile.x(), tile.y(), null);
    }

    public static NavigationPoint np(int x, int y, Direction dir) {
        return new NavigationPoint(x, y, dir);
    }

    public static NavigationPoint np(int x, int y) {
        return new NavigationPoint(x, y, null);
    }

    private final int x;
    private final int y;
    private final Direction dir;

    public NavigationPoint(int x, int y, Direction dir) {
        this.x = x;
        this.y = y;
        this.dir = dir;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public Direction dir() {
        return dir;
    }

    public Vector2i tile() {
        return new Vector2i(x, y);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dir, x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NavigationPoint other = (NavigationPoint) obj;
        return dir == other.dir && x == other.x && y == other.y;
    }
}