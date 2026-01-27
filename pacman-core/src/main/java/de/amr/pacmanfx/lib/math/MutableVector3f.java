/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.math;

/**
 * As there is no *mutable* 3d vector class in standard JDK or JavaFX...
 */
public class MutableVector3f {
    public static final MutableVector3f ZERO = new MutableVector3f(0, 0, 0);

    public float x, y, z;

    public MutableVector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MutableVector3f add(MutableVector3f v) {
        return add(v.x, v.y, v.z);
    }

    public MutableVector3f add(float dx, float dy, float dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public MutableVector3f multiply(float s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public MutableVector3f normalize() {
        float maga = magnitude();
        if (maga > 0) {
            return multiply(1f / maga);
        } else {
            x = y = z = 0; // Point3D.normalized() also return zero vector, so be it
        }
        return this;
    }
}