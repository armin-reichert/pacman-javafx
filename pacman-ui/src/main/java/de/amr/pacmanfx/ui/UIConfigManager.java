/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class UIConfigManager {

    private final Map<String, Supplier<? extends UIConfig>> factories = new HashMap<>();
    private final Map<String, UIConfig> uiConfigCache = new HashMap<>();

    public UIConfigManager() {}

    public void addFactory(String gameVariantName, Supplier<? extends UIConfig> factory) {
        requireNonNull(gameVariantName);
        requireNonNull(factory);
        factories.put(gameVariantName, factory);
    }

    public UIConfig getOrCreateUIConfig(String variantName) {
        requireNonNull(variantName);
        return uiConfigCache.computeIfAbsent(variantName, this::createConfig);
    }

    private UIConfig createConfig(String variantName) {
        try {
            return factories.get(variantName).get();
        } catch (Exception x) {
            throw new RuntimeException("Failed to create config for game variant " + variantName, x);
        }
    }

    public void dispose(String variantName) {
        requireNonNull(variantName);
        final UIConfig config = uiConfigCache.remove(variantName);
        if (config != null) {
            config.dispose();
        }
    }
}
