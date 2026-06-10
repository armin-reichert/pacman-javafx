/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import java.util.HashMap;
import java.util.Map;

public class GameUIExtensions {

    private final Map<String, Object> extensions = new HashMap<>();

    public void addExtension(String key, Object value) {
        extensions.put(key, value);
    }

    public void removeExtension(String key) {
        extensions.remove(key);
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtension(String key, Class<T> type) {
        if (type.isInstance(extensions.get(key))) {
            return (T) extensions.get(key);
        }
        return null;
    }
}
