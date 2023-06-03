/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.animation;

import de.amr.games.pacman.lib.Direction;

/**
 * @author Armin Reichert
 */
public class Turn {

	public record Angles(double from, double to) {
	}

	private static final byte L = 0;
	private static final byte U = 1;
	private static final byte R = 2;
	private static final byte D = 3;

	//@formatter:off
	private static final byte[][][] TURNS = {
		{ null,    {L, R}, {L, U},  {L, -U} }, // LEFT  -> *
		{ {R, L},  null,   {R, U},  {R, D}  }, // RIGHT -> *
		{ {U, L},  {U, R}, null,    {U, D}  }, // UP    -> *
		{ {-U, L}, {D, R}, {-U, U}, null    }, // DOWN  -> *
	};

	private static byte dirIndex(Direction dir) {
		return switch (dir) {	case LEFT -> L;	case RIGHT -> R; case UP -> U; case DOWN -> D; default -> L; };
	}
	//@formatter:on

	public static double angle(Direction dir) {
		return dirIndex(dir) * 90;
	}

	public static Angles angles(Direction fromDir, Direction toDir) {
		var turn = TURNS[fromDir.ordinal()][toDir.ordinal()];
		return new Angles(turn[0] * 90, turn[1] * 90);
	}
}