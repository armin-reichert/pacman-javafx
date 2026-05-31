/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import org.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class ConfigurationsManager {

    private final Map<String, Supplier<? extends UIConfig>> configFactoriesByVariant = new HashMap<>();
    private final Map<String, UIConfig> configs = new HashMap<>();

    public ConfigurationsManager() {}

    public void addConfigFactory(String gameVariantName, Supplier<? extends UIConfig> configFactory) {
        requireNonNull(gameVariantName);
        requireNonNull(configFactory);
        configFactoriesByVariant.put(gameVariantName, configFactory);
    }

    public UIConfig getOrCreateUIConfig(String variantName) {
        requireNonNull(variantName);
        if (!configs.containsKey(variantName)) {
            final var factory = configFactoriesByVariant.get(variantName);
            if (factory == null) {
                throw new IllegalArgumentException("No UIConfig for " + variantName);
            }
            try {
                final var config = factory.get();
                Logger.info("******* Created UIConfig for " + variantName);
                configs.put(variantName, config);
            } catch (Exception x) {
                throw new IllegalArgumentException("Could not create UIConfig for variant " + variantName, x);
            }
        }
        return configs.get(variantName);
    }

    public void dispose(String variantName) {
        requireNonNull(variantName);
        final UIConfig config = configs.remove(variantName);
        if (config != null) {
            config.dispose();
        }
    }
}
