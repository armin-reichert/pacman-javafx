/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.input.ArcadeController;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import de.amr.games.pacman.ui2d.input.Keyboard;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.sound.GameSound;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Game model and controller
    default GameController      gameController() { return GameController.it(); }
    default GameState           gameState() { return gameController().state(); }
    default GameModel           game() { return gameController().currentGame(); }
    default GameVariant         gameVariant() { return game().variant(); }
    void                        selectGameVariant(GameVariant variant);
    default Vector2i            worldSizeInTilesOrElse(Vector2i defaultSize) {
        if (game().world() == null) { return defaultSize; }
        TileMap terrain = game().world().map().terrain();
        return new Vector2i(terrain.numCols(), terrain.numRows());
    }
    void                        setScoreVisible(boolean visible);
    boolean                     isScoreVisible();

    // Input
    Keyboard                    keyboard();
    ArcadeController            arcadeController();
    JoypadKeyBinding            joypadInput();
    void                        enableJoypad();
    void                        disableJoypad();
    void                        nextJoypad();

    // GUI
    void                        selectPage(Page page);
    void                        selectStartPage();
    void                        selectGamePage();
    EditorPage                  getOrCreateEditorPage();
    GamePage                    gamePage();
    default void                showFlashMessage(String message, Object... args) { showFlashMessageSeconds(1, message, args); }
    void                        showFlashMessageSeconds(double seconds, String message, Object... args);
    GameClockFX                 gameClock();

    // Actions
    void                        doFirstCalledGameAction(GameActionProvider actionProvider);
    void                        doFirstCalledGameActionElse(GameActionProvider actionProvider, Runnable defaultAction);

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
    GameSound sound();
    String                      locText(String keyOrPattern, Object... args);
    String                      locGameOverMessage();
    String                      locLevelCompleteMessage();
}