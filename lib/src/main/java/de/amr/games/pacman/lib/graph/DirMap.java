package de.amr.games.pacman.lib.graph;

import java.util.BitSet;

/**
 * A mapping from vertices to directions.
 * <p>
 * Note that each vertex always is mapped to a direction, the default direction is <code>N</code>.
 * 
 * @author Armin Reichert
 */
public class DirMap {

	private final BitSet b0 = new BitSet();
	private final BitSet b1 = new BitSet();

	/**
	 * Gets the direction for the given vertex.
	 * 
	 * @param vertex
	 *                 a vertex
	 * @return the direction for this vertex
	 */
	public Dir get(int vertex) {
		if (b0.get(vertex)) {
			return b1.get(vertex) ? Dir.W : Dir.E;
		}
		else {
			return b1.get(vertex) ? Dir.S : Dir.N;
		}
	}

	/**
	 * Sets the direction for the given vertex.
	 * 
	 * @param vertex
	 *                 a vertex
	 * @param dir
	 *                 a direction
	 */
	public void set(int vertex, Dir dir) {
		switch (dir) {
		case N:
			b1.clear(vertex);
			b0.clear(vertex);
			break;
		case E:
			b1.clear(vertex);
			b0.set(vertex);
			break;
		case S:
			b1.set(vertex);
			b0.clear(vertex);
			break;
		case W:
			b1.set(vertex);
			b0.set(vertex);
			break;
		default:
			throw new IllegalArgumentException("Illegal dir: " + dir);
		}
	}
}