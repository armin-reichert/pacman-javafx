/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.animation;

import de.amr.games.pacman.lib.Direction;

/**
 * @author Armin Reichert
 */
public class Turn {

    private static final byte L = 0, U = 1, R = 2, D = 3;

    public record Angles(double from, double to) {}

	private static byte dirIndex(Direction dir) {
		return switch (dir) {
            case LEFT  -> L;
            case RIGHT -> R;
            case UP    -> U;
            case DOWN  -> D;
        };
	}

    public static double angle(Direction dir) {
        return dirIndex(dir) * 90;
    }
}