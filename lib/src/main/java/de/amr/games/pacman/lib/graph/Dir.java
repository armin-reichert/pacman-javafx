package de.amr.games.pacman.lib.graph;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Dir {
	N, E, S, W;

	private static final Dir[] OPPOSITE = { S, W, N, E };

	public Dir opposite() {
		return OPPOSITE[ordinal()];
	}

	public static Dir random() {
		return values()[new Random().nextInt(4)];
	}

	public static Iterable<Dir> shuffled() {
		List<Dir> dirs = Arrays.asList(Dir.values());
		Collections.shuffle(dirs);
		return dirs;
	}
}