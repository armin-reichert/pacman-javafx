/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameScene_Config;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.assets.UIPreferences;

import java.util.HashMap;
import java.util.Map;

class GameUI_ConfigFactory {
    private final Map<String, Class<? extends GameUI_Config>> configClasses;
    private final Map<String, GameUI_Config> cache = new HashMap<>();
    private final UIPreferences prefs;

    GameUI_ConfigFactory(Map<String, Class<? extends GameUI_Config>> classes, UIPreferences prefs) {
        this.configClasses = classes;
        this.prefs = prefs;
    }

    GameUI_Config getOrCreate(String variantName) {
        return cache.computeIfAbsent(variantName, name -> {
            final Class<?> clazz = configClasses.get(name);
            if (clazz == null) throw new IllegalArgumentException("No config for " + name);
            try {
                final var config = (GameUI_Config) clazz.getConstructor(UIPreferences.class).newInstance(prefs);
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

    void dispose(String variantName) {
        final GameUI_Config config = cache.remove(variantName);
        if (config != null) {
            config.dispose();
        }
    }
}
