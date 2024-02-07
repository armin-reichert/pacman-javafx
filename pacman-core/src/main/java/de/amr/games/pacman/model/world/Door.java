/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.world;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import java.util.Objects;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public final class Door {

	private final Vector2i leftWing;
	private final Vector2i rightWing;

	public Door(Vector2i leftWing, Vector2i rightWing) {
		checkNotNull(leftWing);
		checkNotNull(rightWing);
		this.leftWing = leftWing;
		this.rightWing = rightWing;
	}

	public Vector2i leftWing() {
		return leftWing;
	}

	public Vector2i rightWing() {
		return rightWing;
	}

	/**
	 * @param tile some tile
	 * @return tells if the given tile is occupied by this door
	 */
	public boolean occupies(Vector2i tile) {
		return leftWing.equals(tile) || rightWing.equals(tile);
	}

	/**
	 * @return position where ghost can enter the door
	 */
	public Vector2f entryPosition() {
		return v2f(TS * rightWing.x() - HTS, TS * (rightWing.y() - 1));
	}

	@Override
	public int hashCode() {
		return Objects.hash(leftWing, rightWing);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Door other = (Door) obj;
		return Objects.equals(leftWing, other.leftWing) && Objects.equals(rightWing, other.rightWing);
	}
}