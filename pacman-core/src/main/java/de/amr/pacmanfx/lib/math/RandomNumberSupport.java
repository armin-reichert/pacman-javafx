/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.lib.math;

import java.util.Random;

public interface RandomNumberSupport {

    Random RND = new Random();

    static boolean randomBoolean() {
        return RND.nextBoolean();
    }

    /**
     * @param min left interval bound
     * @param maxExclusive right (open) interval bound
     * @return Random integer number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged to
     * guarantee <code>a<=b</code>
     */
    static int randomInt(int min, int maxExclusive) {
        if (min > maxExclusive) {
            var tmp = min;
            min = maxExclusive;
            maxExclusive = tmp;
        }
        return min + RND.nextInt(maxExclusive - min);
    }

    static byte randomByte(int min, int maxExclusive) {
        if (min < Byte.MIN_VALUE) {
            throw new IllegalArgumentException("Min value %d is out of allowed range %d..%d"
                .formatted(min, Byte.MIN_VALUE, Byte.MAX_VALUE));
        }
        if (maxExclusive > Byte.MAX_VALUE + 1) {
            throw new IllegalArgumentException("Max (exclusive) value %d is out of allowed range %d..%d"
                .formatted(maxExclusive, Byte.MIN_VALUE, Byte.MAX_VALUE+ 1 ));
        }
        return (byte) Math.clamp(randomInt(min, maxExclusive), Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    /**
     * @param min left interval bound
     * @param maxExclusive right (open) interval bound
     * @return Random floating-point number from right-open interval <code>[a; b[</code>. Interval bounds are rearranged
     * to guarantee <code>a<=b</code>
     */
    static float randomFloat(float min, float maxExclusive) {
        if (min > maxExclusive) {
            var tmp = min;
            min = maxExclusive;
            maxExclusive = tmp;
        }
        return min + (maxExclusive - min) * RND.nextFloat();
    }
}
