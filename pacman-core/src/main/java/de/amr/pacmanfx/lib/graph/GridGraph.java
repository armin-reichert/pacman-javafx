/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.lib.graph;

import de.amr.pacmanfx.lib.math.Direction;

/**
 * Grid graph interface as used by maze generation algorithms.
 * 
 * @author Armin Reichert
 */
public interface GridGraph {

	int numRows();

	int numCols();

	int numVertices();

	int numEdges();

	Iterable<Edge> edges();

	int col(int vertex);

	int row(int vertex);

	int vertex(int row, int col);

	int neighbor(int vertex, Direction dir);

	String name(int vertex);

	void connect(int vertex, Direction dir);

	void connect(int vertex, int neighbor);

	void disconnect(int vertex, Direction dir);

	void disconnect(int vertex, int neighbor);

	boolean connected(int vertex, Direction dir);
}