/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class RenderInfo {

    private final Map<String, Object> map = new HashMap<>();

    public RenderInfo() {}

    public boolean getBoolean(String key) {
        return get(key, Boolean.class);
    }

    public <T> T get(String key, Class<T> valueClass) {
        Object value = map.get(key);
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
        map.clear();
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public void putAll(Map<String, Object> otherMap) {
        requireNonNull(otherMap);
        map.putAll(otherMap);
    }
}