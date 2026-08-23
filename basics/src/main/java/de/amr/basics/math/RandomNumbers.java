/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.math;

import java.util.random.RandomGenerator;

import static java.util.Objects.requireNonNull;

/**
 * Utility interface providing convenient static methods for generating random numbers
 * using a shared, thread-safe {@link RandomGenerator}.
 * <p>
 * All methods are thread-safe and use the default {@link RandomGenerator} instance.
 * Methods that accept interval bounds automatically normalize them (swap if necessary)
 * to ensure min ≤ max.
 * </p>
 */
public final class RandomNumbers {

    private RandomNumbers() {}

    /** Shared thread-safe random generator instance. */
    public static final RandomGenerator RANDOM_GENERATOR = RandomGenerator.getDefault();

    /**
     * Returns a random boolean value (true or false) with equal probability (50%).
     *
     * @return {@code true} or {@code false} with equal likelihood
     */
    public static boolean randomBoolean() {
        return RANDOM_GENERATOR.nextBoolean();
    }

    /**
     * Returns {@code true} with the given probability, otherwise {@code false}.
     *
     * @param probability the probability of returning {@code true} (must be in [0.0, 1.0])
     * @return {@code true} with probability p, {@code false} otherwise
     * @throws IllegalArgumentException if probability is not in [0.0, 1.0]
     */
    public static boolean chance(double probability) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException("probability must be in [0.0, 1.0], got: " + probability);
        }
        return RANDOM_GENERATOR.nextDouble() < probability;
    }

    /**
     * Returns a random integer from the right-open interval [min, maxExclusive).
     * <p>
     * If min > maxExclusive, the bounds are automatically swapped.
     * </p>
     *
     * @param min          inclusive lower bound
     * @param maxExclusive exclusive upper bound
     * @return random integer n such that min ≤ n < maxExclusive
     */
    public static int randomInt(int min, int maxExclusive) {
        if (min > maxExclusive) {
            int tmp = min;
            min = maxExclusive;
            maxExclusive = tmp;
        }
        return min + RANDOM_GENERATOR.nextInt(maxExclusive - min);
    }

    /**
     * Returns a random byte value in the right-open interval [min, maxExclusive).
     * <p>
     * Bounds are automatically swapped if necessary.
     * Throws an exception if the resulting range cannot fit into a byte.
     * </p>
     *
     * @param min          inclusive lower bound
     * @param maxExclusive exclusive upper bound
     * @return random byte n such that min ≤ n < maxExclusive
     * @throws IllegalArgumentException if the range cannot be represented as a byte
     */
    public static byte randomByte(int min, int maxExclusive) {
        if (min < Byte.MIN_VALUE) {
            throw new IllegalArgumentException(
                    "min value %d is below allowed range %d..%d".formatted(min, Byte.MIN_VALUE, Byte.MAX_VALUE));
        }
        if (maxExclusive > Byte.MAX_VALUE + 1) {
            throw new IllegalArgumentException(
                    "maxExclusive value %d is above allowed range %d..%d".formatted(maxExclusive, Byte.MIN_VALUE, Byte.MAX_VALUE + 1));
        }
        return (byte) Math.clamp(randomInt(min, maxExclusive), Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    /**
     * Returns a random float value in the right-open interval [min, maxExclusive).
     * <p>
     * If min > maxExclusive, the bounds are automatically swapped.
     * </p>
     *
     * @param min          inclusive lower bound
     * @param maxExclusive exclusive upper bound
     * @return random float f such that min ≤ f < maxExclusive
     */
    public static float randomFloat(float min, float maxExclusive) {
        if (min > maxExclusive) {
            float tmp = min;
            min = maxExclusive;
            maxExclusive = tmp;
        }
        return min + (maxExclusive - min) * RANDOM_GENERATOR.nextFloat();
    }

    /**
     * Returns a random element from the given byte array.
     * <p>
     * The array must not be {@code null} and must contain at least one element.
     * A uniformly distributed random index in the interval [0, array.length) is selected.
     * </p>
     *
     * @param array the array to select an element from (must not be {@code null})
     * @return a randomly selected byte from the array
     * @throws NullPointerException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array.length == 0}
     */
    public static byte randomByteArrayElement(byte[] array) {
        requireNonNull(array);
        if (array.length == 0) {
            throw new IllegalArgumentException("array must not be empty");
        }
        return array[randomInt(0, array.length)];
    }

    /**
     * Returns a random element from the given array.
     * <p>
     * The array must not be {@code null} and must contain at least one element.
     * A uniformly distributed random index in the interval [0, array.length) is selected.
     * </p>
     *
     * @param <T>   the element type
     * @param array the array to select an element from (must not be {@code null})
     * @return a randomly selected element from the array
     * @throws NullPointerException if {@code array} is {@code null}
     * @throws IllegalArgumentException if {@code array.length == 0}
     */
    public static <T> T randomArrayEntry(T[] array) {
        requireNonNull(array);
        if (array.length == 0) {
            throw new IllegalArgumentException("array must not be empty");
        }
        return array[randomInt(0, array.length)];
    }
}