/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import java.util.HashMap;
import java.util.Map;

public class GameExtensions {

    private final Map<String, Object> extensionMap = new HashMap<>();

    public void add(String key, Object value) {
        extensionMap.put(key, value);
    }

    public void remove(String key) {
        extensionMap.remove(key);
    }

    public Map<String, Object> getExtensionMap() {
        return extensionMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        if (type.isInstance(extensionMap.get(key))) {
            return (T) extensionMap.get(key);
        }
        return null;
    }
}
