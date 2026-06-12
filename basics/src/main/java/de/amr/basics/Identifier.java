/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public interface Identifier {
    String name();

    default boolean identifies(Identifier thing) {
        requireNonNull(thing);
        return thing.name().equals(name());
    }

    default boolean nameIsOneOf(String... names) {
        if (names.length == 0) return false;
        return Stream.of(names).anyMatch(name -> name().equals(name));
    }
}
