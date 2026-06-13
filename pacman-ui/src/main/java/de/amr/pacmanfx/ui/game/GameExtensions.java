/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.Identifier;

import java.util.HashMap;
import java.util.Map;

public class GameExtensions {

    private final Map<Identifier, Object> extensionMap = new HashMap<>();

    public void add(Identifier identifier, Object value) {
        extensionMap.put(identifier, value);
    }

    public void remove(Identifier identifier) {
        extensionMap.remove(identifier);
    }

    public Map<Identifier, Object> getExtensionMap() {
        return extensionMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Identifier identifier, Class<T> type) {
        if (type.isInstance(extensionMap.get(identifier))) {
            return (T) extensionMap.get(identifier);
        }
        return null;
    }
}
