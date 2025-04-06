/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.GameScene2D;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.ui.Globals.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class GameUIConfigManager {

    protected final Map<GameVariant, GameUIConfig> configMap = new EnumMap<>(GameVariant.class);

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param uiConfig the UI configuration for this variant
     */
    public void set(GameVariant variant, GameUIConfig uiConfig) {
        assertNotNull(variant);
        assertNotNull(uiConfig);
        uiConfig.gameScenes().forEach(scene -> {
            if (scene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
            }
        });
        configMap.put(variant, uiConfig);
    }

    public GameUIConfig configuration(GameVariant gameVariant) {
        return configMap.get(gameVariant);
    }

    public GameUIConfig current() {
        return configMap.get(THE_GAME_CONTROLLER.selectedGameVariant());
    }

    public boolean currentGameSceneIsPlayScene2D() {
        Optional<GameScene> currentGameScene = THE_UI.currentGameScene();
        return currentGameScene.isPresent()
            && current().gameSceneHasID(currentGameScene.get(), "PlayScene2D");
    }

    public boolean currentGameSceneIsPlayScene3D() {
        Optional<GameScene> currentGameScene = THE_UI.currentGameScene();
        return currentGameScene.isPresent()
            && current().gameSceneHasID(currentGameScene.get(), "PlayScene3D");
    }

    public boolean currentGameSceneIs2D() {
        return THE_UI.currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }
}
