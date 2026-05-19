/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.basics.math;

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

    public Vector3f plus(Vector3f v) {
        return v == ZERO ? this : new Vector3f(x + v.x, y + v.y, z + v.z);
    }

    public Vector3f minus(Vector3f v) {
        return v == ZERO ? this : new Vector3f(x - v.x, y - v.y, z - v.z);
    }

    public Vector3f mul(float s) {
        return s == 0 ? ZERO : new Vector3f(s*x, s*y, s*z);
    }

    /**
     * Computes the dot product of this vector and the given vector.
     *
     * @param v other vector
     * @return the dot product
     */
    public float dot(Vector3f v) {
        return v == ZERO ? 0 : x * v.x + y * v.y + z * v.z;
    }

    /**
     * @return the length of this vector
     */
    public float length() {
        return this == ZERO ? 0 : (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * @return normalized (length 1) vector
     */
    public Vector3f normalized() {
        return setToLength(1);
    }

    /**
     * @param length the new length
     * @return This vector scaled to the given length.
     *         If this vector is the zero vector or the new length is 0, returns the zero vector.
     */
    public Vector3f setToLength(float length) {
        if (this == ZERO) return this;
        final float myLength = length();
        return myLength == 0 || length == 0 ? ZERO : mul(length / myLength);
    }

    public double euclideanDist(Vector3f v) {
        return minus(v).length();
    }
}