/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.math;

public final class MathAdds {

    private MathAdds() {}

    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    public static boolean isOdd(int n) {
        return !isEven(n);
    }

    /**
     * @param a left interval bound
     * @param b right interval bound
     * @param t "time" between 0 and 1
     * @return linear interpolation between {@code a} and {@code b}
     */
    public static double lerp(double a, double b, double t) {
        return Math.fma(Math.clamp(t, 0, 1), b - a, a);
    }

    /**
     * @param a left interval bound
     * @param b right interval bound
     * @param t "time" between 0 and 1
     * @return linear interpolation between {@code a} and {@code b}
     */
    public static float lerp(float a, float b, float t) {
        return Math.fma(Math.clamp(t, 0.0f, 1.0f), b - a, a);
    }
}
