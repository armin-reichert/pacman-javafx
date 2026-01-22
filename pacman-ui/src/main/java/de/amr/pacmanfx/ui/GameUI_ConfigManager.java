/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui._2d.GameScene2D;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GameUI_ConfigManager {

    private final Map<String, Supplier<? extends GameUI_Config>> factories;
    private final Map<String, GameUI_Config> cache = new HashMap<>();

    public GameUI_ConfigManager(Map<String, Supplier<? extends GameUI_Config>> factories) {
        this.factories = factories;
    }

    public GameUI_Config getOrCreate(String variantName) {
        return cache.computeIfAbsent(variantName, name -> {
            try {
                final var config = factories.get(variantName).get();
                // UI configuration class must also implement GameScene_Config interface
                final GameScene_Config sceneConfig = (GameScene_Config) config;
                sceneConfig.gameScenes().forEach(scene -> {
                    if (scene instanceof GameScene2D gameScene2D) {
                        gameScene2D.debugInfoVisibleProperty().bind(GameUI.PROPERTY_DEBUG_INFO_VISIBLE);
                    }
                });
                return config;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create config for " + name, e);
            }
        });
    }

    public void dispose(String variantName) {
        final GameUI_Config config = cache.remove(variantName);
        if (config != null) {
            config.dispose();
        }
    }
}
