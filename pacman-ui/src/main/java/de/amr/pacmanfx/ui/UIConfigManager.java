/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui._2d.GameScene2D;
import org.tinylog.Logger;

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

    public UIConfig getOrCreate(String variantName) {
        requireNonNull(variantName);
        return uiConfigCache.computeIfAbsent(variantName, _ -> {
            try {
                final UIConfig config = factories.get(variantName).get();
                if (config instanceof GameSceneConfig gameSceneConfig) {
                    gameSceneConfig.gameScenes().forEach(scene -> {
                        if (scene instanceof GameScene2D gameScene2D) {
                            gameScene2D.debugInfoVisibleProperty().bind(GameUI.PROPERTY_DEBUG_INFO_VISIBLE);
                        }
                    });
                } else {
                    Logger.error("UI configuration class must also implement GameScene_Config interface");
                }
                return config;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create config for " + variantName, e);
            }
        });
    }

    public void dispose(String variantName) {
        requireNonNull(variantName);
        final UIConfig config = uiConfigCache.remove(variantName);
        if (config != null) {
            config.dispose();
        }
    }
}
