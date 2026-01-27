/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.lib.graph;

/**
 * @author Armin Reichert
 */
public record Edge(GridGraph grid, int either, int other, int weight) implements Comparable<Edge> {

	public Edge(GridGraph grid, int either, int other) {
		this(grid, either, other, 0);
	}

	@Override
	public int compareTo(Edge other) {
		return Integer.compare(weight, other.weight);
	}

	@Override
	public String toString() {
		return String.format("%s->%s(%d)", grid.name(either), grid.name(other), weight);
	}
}