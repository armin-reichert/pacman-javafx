/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

/**
 * Immutable 3D vector with float precision. No full-fledged implementation, just the needed methods.
 *
 * @author Armin Reichert
 */
public record Vector3f(float x, float y, float z) {

    public static final Vector3f ZERO = new Vector3f(0, 0, 0);

    public Vector3f(double x, double y, double z) {
        this((float) x, (float) y, (float) z);
    }

    public Vector3f(Vector3f v) {
        this(v.x, v.y, v.z);
    }

    public Vector3f add(Vector3f v) {
        return new Vector3f(x + v.x, y + v.y, z + v.z);
    }

    public Vector3f mul(float s) {
        return new Vector3f(s*x, s*y, s*z);
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