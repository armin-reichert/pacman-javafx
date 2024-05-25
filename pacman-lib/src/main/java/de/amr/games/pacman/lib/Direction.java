/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * The move directions inside the world.
 *
 * @author Armin Reichert
 */
public enum Direction {

    LEFT(-1, 0), RIGHT(1, 0), UP(0, -1), DOWN(0, 1);

    public static Stream<Direction> stream() {
        return Stream.of(values());
    }

    public static List<Direction> shuffled() {
        List<Direction> dirs = Arrays.asList(values());
        Collections.shuffle(dirs);
        return dirs;
    }

    private final Vector2i vector;

    Direction(int x, int y) {
        vector = new Vector2i(x, y);
    }

    public Vector2i vector() {
        return vector;
    }

    public Direction opposite() {
        return switch (this) {
            case UP -> DOWN;
            case LEFT -> RIGHT;
            case DOWN -> UP;
            case RIGHT -> LEFT;
        };
    }

    public Direction nextCounterClockwise() {
        return switch (this) {
            case UP -> LEFT;
            case LEFT -> DOWN;
            case DOWN -> RIGHT;
            case RIGHT -> UP;
        };
    }

    public Direction nextClockwise() {
        return switch (this) {
            case UP -> RIGHT;
            case RIGHT -> DOWN;
            case DOWN -> LEFT;
            case LEFT -> UP;
        };
    }

    public boolean isVertical() {
        return this == UP || this == DOWN;
    }

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public boolean sameOrientation(Direction other) {
        return isHorizontal() && other.isHorizontal();
    }
}