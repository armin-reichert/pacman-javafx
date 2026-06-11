/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import java.util.HashMap;
import java.util.Map;

public class GameUI_Extensions {

    private final Map<String, Object> extensionMap = new HashMap<>();

    public void addExtension(String key, Object value) {
        extensionMap.put(key, value);
    }

    public void removeExtension(String key) {
        extensionMap.remove(key);
    }

    public Map<String, Object> getExtensions() {
        return extensionMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtension(String key, Class<T> type) {
        if (type.isInstance(extensionMap.get(key))) {
            return (T) extensionMap.get(key);
        }
        return null;
    }
}
