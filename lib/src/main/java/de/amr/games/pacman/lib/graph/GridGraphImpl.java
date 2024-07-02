package de.amr.games.pacman.lib.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Stripped down grid graph implementation.
 * 
 * @author Armin Reichert
 */
public class GridGraphImpl implements GridGraph {

	private final int rows;
	private final int cols;
	private final BitSet edges;

	public GridGraphImpl(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
		edges = new BitSet(4 * rows * cols);
	}

	@Override
	public int numCols() {
		return cols;
	}

	@Override
	public int numRows() {
		return rows;
	}

	@Override
	public int numVertices() {
		return rows * cols;
	}

	@Override
	public int numEdges() {
		return edges.cardinality() / 2;
	}

	@Override
	public int vertex(int row, int col) {
		return row * cols + col;
	}

	@Override
	public int row(int vertex) {
		return vertex / cols;
	}

	@Override
	public int col(int vertex) {
		return vertex % cols;
	}

	@Override
	public int neighbor(int vertex, Dir dir) {
		int row = row(vertex);
		int col = col(vertex);
		switch (dir) {
		case N:
			return row - 1 >= 0 ? vertex(row - 1, col) : -1;
		case E:
			return col + 1 < cols ? vertex(row, col + 1) : -1;
		case S:
			return row + 1 < rows ? vertex(row + 1, col) : -1;
		case W:
			return col - 1 >= 0 ? vertex(row, col - 1) : -1;
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean connected(int vertex, Dir dir) {
		return edges.get(4 * vertex + dir.ordinal());
	}

	@Override
	public void connect(int vertex, Dir dir) {
		if (connected(vertex, dir)) {
			throw new IllegalStateException(String.format("Already connected: %s, %s", name(vertex), dir));
		}
		int neighbor = neighbor(vertex, dir);
		if (neighbor == -1) {
			throw new IllegalArgumentException(
					String.format("Cannot connect vertex %s towards %s", name(vertex), dir.name()));
		}
		edges.set(4 * vertex + dir.ordinal());
		edges.set(4 * neighbor + dir.opposite().ordinal());
	}

	@Override
	public void connect(int vertex, int neighbor) {
		for (Dir dir : Dir.values()) {
			if (neighbor == neighbor(vertex, dir)) {
				connect(vertex, dir);
				return;
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public void disconnect(int vertex, Dir dir) {
		if (!connected(vertex, dir)) {
			throw new IllegalStateException(String.format("Not connected: %s, %s", name(vertex), dir));
		}
		int neighbor = neighbor(vertex, dir);
		if (neighbor == -1) {
			throw new IllegalArgumentException(
					String.format("Cannot disconnect vertex %s towards %s", name(vertex), dir.name()));
		}
		edges.clear(4 * vertex + dir.ordinal());
		edges.clear(4 * neighbor + dir.opposite().ordinal());
	}

	@Override
	public void disconnect(int vertex, int neighbor) {
		for (Dir dir : Dir.values()) {
			if (neighbor == neighbor(vertex, dir)) {
				disconnect(vertex, dir);
				return;
			}
		}
		throw new IllegalStateException();
	}

	@Override
	public Iterable<Edge> edges() {
		List<Edge> edgeList = new ArrayList<>();
		for (int vertex = 0; vertex < numVertices(); ++vertex) {
			for (Dir dir : new Dir[] { Dir.E, Dir.S }) {
				if (connected(vertex, dir)) {
					edgeList.add(new Edge(this, vertex, neighbor(vertex, dir)));
				}
			}
		}
		return edgeList;
	}

	@Override
	public String name(int vertex) {
		return String.format("(%d,%d)", row(vertex), col(vertex));
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Rows: ").append(rows).append("\n");
		sb.append("Cols: ").append(cols).append("\n");
		for (Edge edge : edges()) {
			sb.append(name(edge.either())).append("->").append(name(edge.other())).append("\n");
		}
		return sb.toString();
	}
}