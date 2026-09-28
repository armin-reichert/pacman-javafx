/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.HashMap;

import static java.util.Objects.requireNonNull;

public class MapWithAccessors extends HashMap<Object, Object> {

    public static final MapWithAccessors EMPTY = new MapWithAccessors();

    public boolean getBoolean(Object key) {
        requireNonNull(key);
        final Boolean b = get(key, Boolean.class);
        return b != null && b;
    }

    public <T> T get(Object key, Class<T> expectedValueClass) {
        requireNonNull(key);
        requireNonNull(expectedValueClass);

        final Object value = get(key);
        if (value == null) {
            return null;
        }
        if (expectedValueClass.isInstance(value)) {
            return expectedValueClass.cast(value);
        }
        throw new IllegalArgumentException("Key '%s' is not mapped to a value of class '%s'"
            .formatted(key, expectedValueClass.getSimpleName()));
    }
}