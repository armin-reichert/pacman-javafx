/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.sound.GameSounds;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Game model and controller
    default GameController      gameController() { return GameController.it(); }
    default GameState           gameState() { return GameController.it().state(); }
    ObjectProperty<GameVariant> gameVariantProperty();
    default GameVariant         gameVariant() { return GameController.it().currentGame().variant(); }
    void                        selectGameVariant(GameVariant variant);
    default GameModel           game() { return GameController.it().currentGame(); }
    void                        setScoreVisible(boolean visible);
    boolean                     isScoreVisible();
    default Vector2i            worldSizeInTiles(GameWorld world, Vector2i defaultSize) {
        return world != null ? new Vector2i(world.map().terrain().numCols(), world.map().terrain().numRows()) : defaultSize;
    }

    // UI
    Keyboard                    keyboard();
    void                        selectPage(Page page);
    void                        selectStartPage();
    void                        selectGamePage();
    EditorPage                  getOrCreateEditorPage();
    GamePage                    gamePage();
    default void                showFlashMessage(String message, Object... args) { showFlashMessageSeconds(1, message, args); }
    void                        showFlashMessageSeconds(double seconds, String message, Object... args);
    GameClockFX                 gameClock();

    // Actions
    void                        doFirstCalledAction(ActionProvider actionProvider);
    void                        doFirstCalledActionElse(ActionProvider actionProvider, Runnable defaultAction);

    // Game scenes
    GameSceneConfig             gameSceneConfig(GameVariant variant);
    default GameSceneConfig     currentGameSceneConfig() { return gameSceneConfig(gameVariant()); }
    boolean                     currentGameSceneHasID(String gameSceneID);
    ObjectProperty<GameScene>   gameSceneProperty();
    Optional<GameScene>         currentGameScene();
    void                        updateGameScene(boolean reloadCurrent);
    default void                updateRenderer() { currentGameSceneConfig().renderer().update(game()); }

    // Resources
    AssetStorage                assets();
    GameSounds                  sounds();
    String                      locText(String keyOrPattern, Object... args);
}