/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.math;

public final class MoreMath {

    private MoreMath() {}

    public static boolean isEven(int n) {
        return n % 2 == 0;
    }

    public static boolean isOdd(int n) {
        return n % 2 != 0;
    }

    /**
     * @param from value1
     * @param to value2
     * @param t      "time" between 0 and 1
     * @return linear interpolation between {@code from} and {@code to} values
     */
    public static double lerp(double from, double to, double t) {
        return (1 - t) * from + t * to;
    }
}
