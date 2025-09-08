package de.amr.pacmanfx.lib.graph;

import de.amr.pacmanfx.lib.Direction;

import java.util.BitSet;

/**
 * A mapping from vertices to directions.
 * <p>
 * Note that each vertex always is mapped to a direction, the default direction is <code>Direction.UP</code>.
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
	public Direction get(int vertex) {
		if (b0.get(vertex)) {
			return b1.get(vertex) ? Direction.LEFT : Direction.RIGHT;
		}
		else {
			return b1.get(vertex) ? Direction.DOWN : Direction.UP;
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
	public void set(int vertex, Direction dir) {
		switch (dir) {
			case UP:
			b1.clear(vertex);
			b0.clear(vertex);
			break;
			case RIGHT:
			b1.clear(vertex);
			b0.set(vertex);
			break;
			case DOWN:
			b1.set(vertex);
			b0.clear(vertex);
			break;
			case LEFT:
			b1.set(vertex);
			b0.set(vertex);
			break;
		default:
			throw new IllegalArgumentException("Illegal dir: " + dir);
		}
	}
}