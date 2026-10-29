/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class ValueMap {

    public static boolean DEFAULT_BOOLEAN_VALUE = false;
    public static int     DEFAULT_INT_VALUE = 0;
    public static long    DEFAULT_LONG_VALUE = 0;

    public static ValueMap singletonMap(Object key, Object value) {
        return new ValueMap(Map.of(key, value));
    }

    public static ValueMap create() {
        return new ValueMap(new HashMap<>());
    }

    public static final ValueMap EMPTY_VALUE_MAP = new ValueMap(Map.of()) {
        @Override
        public <T> T value(Object key, Class<T> expectedType) {
            throw new UnsupportedOperationException("Cannot get value from empty immutable map");
        }

        @Override
        public void store(Object key, Object value) {
            throw new UnsupportedOperationException("Cannot put value into empty immutable map");
        }
    };

    private final Map<Object, Object> map;

    protected ValueMap(Map<Object, Object> map) {
        this.map = requireNonNull(map);
    }

    public boolean booleanValue(Named key) {
        final Boolean value = value(key, Boolean.class);
        return value != null ? value : DEFAULT_BOOLEAN_VALUE;
    }

    public int intValue(Named key) {
        final Integer value = value(key, Integer.class);
        return value != null ? value : DEFAULT_INT_VALUE;
    }

    public long longValue(Named key) {
        final Long value = value(key, Long.class);
        return value != null ? value : DEFAULT_LONG_VALUE;
    }

    public <T> T value(Object key, Class<T> expectedType) {
        requireNonNull(key);
        requireNonNull(expectedType);

        final Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (expectedType.isInstance(value)) {
            return expectedType.cast(value);
        }
        throw new IllegalArgumentException("Key '%s' is not mapped to a value of class '%s'"
            .formatted(key, expectedType.getSimpleName()));
    }

    public void store(Object key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        map.put(key, value);
    }

    public void remove(Named key) {
        requireNonNull(key);
        map.remove(key);
    }
}