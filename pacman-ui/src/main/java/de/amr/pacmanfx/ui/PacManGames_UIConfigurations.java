/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.uilib.GameScene;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames_Env.PY_DEBUG_INFO_VISIBLE;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;
import static java.util.Objects.requireNonNull;

public class PacManGames_UIConfigurations {

    protected final Map<GameVariant, PacManGames_UIConfiguration> configMap = new EnumMap<>(GameVariant.class);

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param uiConfig the UI configuration for this variant
     */
    public void set(GameVariant variant, PacManGames_UIConfiguration uiConfig) {
        requireNonNull(variant);
        requireNonNull(uiConfig);
        uiConfig.gameScenes().forEach(scene -> {
            if (scene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
            }
        });
        configMap.put(variant, uiConfig);
    }

    public PacManGames_UIConfiguration configuration(GameVariant gameVariant) {
        return configMap.get(gameVariant);
    }

    public PacManGames_UIConfiguration currentConfig() {
        return configMap.get(theGameController().selectedGameVariant());
    }

    public boolean currentGameSceneIsPlayScene2D() {
        Optional<GameScene> currentGameScene = theUI().currentGameScene();
        return currentGameScene.isPresent()
            && currentConfig().gameSceneHasID(currentGameScene.get(), "PlayScene2D");
    }

    public boolean currentGameSceneIsPlayScene3D() {
        Optional<GameScene> currentGameScene = theUI().currentGameScene();
        return currentGameScene.isPresent()
            && currentConfig().gameSceneHasID(currentGameScene.get(), "PlayScene3D");
    }

    public boolean currentGameSceneIs2D() {
        return theUI().currentGameScene().map(GameScene2D.class::isInstance).orElse(false);
    }

    /**
     * @param config UI configuration
     * @param sceneBefore scene displayed before switching
     * @param sceneAfter scene displayed after switching
     * @return <code>23</code> if 2D -> 3D switch, <code>32</code> if 3D -> 2D switch</code>,
     *  <code>0</code> if scene before switch is not yet available
     */
    public byte identifySceneSwitchType(PacManGames_UIConfiguration config, GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case null -> 0; // may happen, it's ok
            case GameScene2D gameScene2D when sceneAfter instanceof PlayScene3D -> 23;
            case PlayScene3D playScene3D when sceneAfter instanceof GameScene2D -> 32;
            default -> 0;
        };
    }
}
