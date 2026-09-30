/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class InfoMap {

    public static InfoMap create() {
        return new InfoMap(new HashMap<>());
    }

    public static final InfoMap EMPTY_IMMUTABLE_MAP = new InfoMap(Map.of()) {
        @Override
        public boolean getBoolean(Object key) {
            throw new UnsupportedOperationException("Cannot get value from empty immutable map");
        }

        @Override
        public <T> T get(Object key, Class<T> expectedValueClass) {
            throw new UnsupportedOperationException("Cannot get value from empty immutable map");
        }

        @Override
        public void put(Object key, Object value) {
            throw new UnsupportedOperationException("Cannot put value into empty immutable map");
        }
    };

    private Map<Object, Object> map = new HashMap<Object, Object>();

    private InfoMap(Map<Object, Object> map) {
        this.map = map;
    }

    public boolean getBoolean(Object key) {
        requireNonNull(key);
        final Boolean b = get(key, Boolean.class);
        return b != null && b;
    }

    public <T> T get(Object key, Class<T> expectedValueClass) {
        requireNonNull(key);
        requireNonNull(expectedValueClass);

        final Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (expectedValueClass.isInstance(value)) {
            return expectedValueClass.cast(value);
        }
        throw new IllegalArgumentException("Key '%s' is not mapped to a value of class '%s'"
            .formatted(key, expectedValueClass.getSimpleName()));
    }

    public void put(Object key, Object value) {
        requireNonNull(key);
        requireNonNull(value);
        map.put(key, value);
    }
}