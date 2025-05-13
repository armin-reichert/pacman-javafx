/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.gameVariant;
import static java.util.Objects.requireNonNull;

public class GameUIConfigManager {

    protected final Map<GameVariant, GameUIConfig> configMap = new EnumMap<>(GameVariant.class);

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param uiConfig the UI configuration for this variant
     */
    public void set(GameVariant variant, GameUIConfig uiConfig) {
        requireNonNull(variant);
        requireNonNull(uiConfig);
        uiConfig.gameScenes().forEach(scene -> {
            if (scene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().bind(PacManGamesEnv.PY_DEBUG_INFO_VISIBLE);
            }
        });
        configMap.put(variant, uiConfig);
    }

    public GameUIConfig configuration(GameVariant gameVariant) {
        return configMap.get(gameVariant);
    }

    public GameUIConfig current() {
        return configMap.get(gameVariant());
    }

    public boolean currentGameSceneIsPlayScene2D() {
        Optional<GameScene> currentGameScene = PacManGamesEnv.theUI().currentGameScene();
        return currentGameScene.isPresent()
            && current().gameSceneHasID(currentGameScene.get(), "PlayScene2D");
    }

    public boolean currentGameSceneIsPlayScene3D() {
        Optional<GameScene> currentGameScene = PacManGamesEnv.theUI().currentGameScene();
        return currentGameScene.isPresent()
            && current().gameSceneHasID(currentGameScene.get(), "PlayScene3D");
    }

    public boolean currentGameSceneIs2D() {
        return PacManGamesEnv.theUI().currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    public boolean is2D3DPlaySceneSwitch(GameUIConfig config, GameScene oldScene, GameScene newScene) {
        if (oldScene == null && newScene == null) {
            throw new IllegalStateException("WTF is going on here, both game scenes are NULL!");
        }
        if (oldScene == null) {
            return false; // may happen, it's ok
        }
        return config.gameSceneHasID(oldScene, "PlayScene2D") && config.gameSceneHasID(newScene, "PlayScene3D")
            || config.gameSceneHasID(oldScene, "PlayScene3D") && config.gameSceneHasID(newScene, "PlayScene2D");
    }
}
