/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2i;

import java.util.Objects;

/**
 * A portal connects two tunnel ends leading out of the map.
 * <p>
 * This kind of portal prolongates the right tunnel end by <code>DEPTH</code> tiles before wrapping with the left part
 * (also <code>DEPTH</code> tiles) of the portal.
 * 
 * @author Armin Reichert
 */
public final class Portal {

	private final Vector2i leftTunnelEnd;
	private final Vector2i rightTunnelEnd;
	private final int depth;

	public Portal(Vector2i leftTunnelEnd, Vector2i rightTunnelEnd, int depth) {
		this.leftTunnelEnd = leftTunnelEnd;
		this.rightTunnelEnd = rightTunnelEnd;
		this.depth = depth;
	}

	public Vector2i leftTunnelEnd() {
		return leftTunnelEnd;
	}

	public Vector2i rightTunnelEnd() {
		return rightTunnelEnd;
	}

	public int depth() {
		return depth;
	}

	public boolean contains(Vector2i tile) {
		for (int i = 1; i <= depth; ++i) {
			if (tile.equals(leftTunnelEnd.minus(i, 0))) {
				return true;
			}
			if (tile.equals(rightTunnelEnd.plus(i, 0))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(depth, leftTunnelEnd, rightTunnelEnd);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Portal other = (Portal) obj;
		return depth == other.depth && Objects.equals(leftTunnelEnd, other.leftTunnelEnd)
				&& Objects.equals(rightTunnelEnd, other.rightTunnelEnd);
	}
}