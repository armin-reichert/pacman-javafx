/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class InfoMap {

    // create on-access
    private Map<Object, Object> entries;

    public InfoMap() {}

    public boolean getBoolean(Object key) {
        final Boolean b = get(key, Boolean.class);
        return b != null && b;
    }

    public <T> T get(Object key, Class<T> valueClass) {
        Object value = entries().get(key);
        if (value == null) {
            return null;
        }
        if (valueClass.isInstance(value)) {
            return valueClass.cast(value);
        }
        throw new IllegalArgumentException("Key '%s' is not assigned to value of class '%s'"
                .formatted(key, valueClass.getSimpleName()));
    }

    public void clear() {
        entries().clear();
    }

    public void put(Object key, Object value) {
        entries().put(key, value);
    }

    public void putAll(InfoMap infoMap) {
        entries().putAll(infoMap.entries());
    }

    public void putAll(Map<Object, Object> otherMap) {
        requireNonNull(otherMap);
        entries().putAll(otherMap);
    }

    public Map<Object, Object> entries() {
        if (entries == null) {
            entries = new HashMap<>();
        }
        return entries;
    }
}