/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.objimport;

import de.amr.games.pacman.lib.Vector2f;

/**
 * Immutable 3D vector with float precision. No full-fledged implementation, just the needed methods.
 * 
 * @author Armin Reichert
 */
record Vector3f(float x, float y, float z) {

	public Vector3f(Vector3f v) {
		this(v.x, v.y, v.z);
	}

	public Vector3f(Vector2f v, float z) {
		this(v.x(), v.y(), z);
	}

	public Vector2f toVector2f() {
		return new Vector2f(x, y);
	}

	/**
	 * Computes the dot product of this vector and the given vector.
	 *
	 * @param v other vector
	 * @return the dot product
	 */
	public float dot(Vector3f v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/**
	 * @return the length of this vector
	 */
	public float length() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	/**
	 * @return normalized (length 1) vector
	 */
	public Vector3f normalized() {
		float norm = 1.0f / length();
		return new Vector3f(x * norm, y * norm, z * norm);
	}
}