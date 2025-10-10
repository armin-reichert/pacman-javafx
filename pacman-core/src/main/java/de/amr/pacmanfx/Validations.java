/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.model.GameException;

import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public interface Validations {

    Pattern PATTERN_IDENTIFIER = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*$");

    static byte requireValidGhostPersonality(byte id) {
        if (id < 0 || id > 3) throw GameException.invalidGhostPersonality(id);
        return id;
    }

    static int requireValidLevelNumber(int number) {
        if (number < 1) throw GameException.invalidLevelNumber(number);
        return number;
    }

    static double requireNonNegative(double value, String messageFormat) {
        if (value < 0) throw new IllegalArgumentException(String.format(messageFormat, value));
        return value;
    }

    static double requireNonNegative(double value) {
        return requireNonNegative(value, "%f must be zero or positive");
    }

    static int requireNonNegativeInt(int value) {
        if (value < 0) throw new IllegalArgumentException("Non-negative integer required, but got %d".formatted(value));
        return value;
    }

    static String requireValidIdentifier(String s) {
        requireNonNull(s);
        if (PATTERN_IDENTIFIER.matcher(s).matches()) {
            return s;
        }
        throw new IllegalArgumentException("'%s' is no valid identifier".formatted(s));
    }

    /**
     * @param value some value
     * @param from lower bound (inclusive)
     * @param to upper bound (inclusive)
     * @return {@code true} if value is in closed interval {@code [from; to]}
     */
    static boolean inClosedRange(long value, long from, long to) {
        return from <= value && value <= to;
    }

    /**
     * @param delta  maximum allowed deviation (non-negative number)
     * @param value  value
     * @param target target value
     * @return {@code true} if the given values differ at most by the given difference
     */
    static boolean differsAtMost(double delta, double value, double target) {
        if (delta < 0) {
            throw new IllegalArgumentException(String.format("Difference must not be negative but is %f", delta));
        }
        return value >= (target - delta) && value <= (target + delta);
    }

    @SafeVarargs
    static <T> boolean stateIsOneOf(T value, T... alternatives) {
        if (value == null) {
            throw new IllegalArgumentException("Value is null");
        }
        if (alternatives.length == 0) {
            throw new IllegalArgumentException("No alternatives given");
        }
        return Set.of(alternatives).contains(value);
    }
}
