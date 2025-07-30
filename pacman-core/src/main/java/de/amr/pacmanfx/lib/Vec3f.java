/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.lib;

/**
 * As there is no *mutable* 3d vector class in standard JDK or JavaFX...
 */
public class Vec3f {
    public static final Vec3f ZERO = new Vec3f(0, 0, 0);

    public float x, y, z;

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f add(Vec3f v) {
        return add(v.x, v.y, v.z);
    }

    public Vec3f add(float dx, float dy, float dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    public Vec3f multiply(float s) {
        x *= s;
        y *= s;
        z *= s;
        return this;
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vec3f normalize() {
        float maga = magnitude();
        if (maga > 0) {
            return multiply(1f / maga);
        } else {
            x = y = z = 0; // Point3D.normalized() also return zero vector, so be it
        }
        return this;
    }
}