/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.input.ArcadeKeyAdapter;
import de.amr.games.pacman.ui2d.input.JoypadKeyAdapter;
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

    // Assets
    AssetStorage assets();
    GameSound sound();
    String locText(String keyOrPattern, Object... args);
    String locGameOverMessage();
    String locLevelCompleteMessage();

    // Game model and controller
    GameClockFX gameClock();
    default long tick() { return gameClock().getTickCount(); }
    default GameController gameController() { return GameController.it(); }
    default GameState gameState() { return gameController().state(); }
    default GameModel game() { return gameController().currentGame(); }
    /** @return game level (which must exist!) */
    default GameLevel level() { return game().level().orElseThrow(); }
    default GameVariant gameVariant() { return gameController().currentGameVariant(); }
    void selectGameVariant(GameVariant variant);

    // Input
    Keyboard keyboard();
    ArcadeKeyAdapter arcade();
    JoypadKeyAdapter joypad();
    void enableJoypad();
    void disableJoypad();
    void nextJoypad();

    // GUI
    void setScoreVisible(boolean visible);
    boolean isScoreVisible();
    void selectPage(Page page);
    void selectStartPage();
    void selectGamePage();
    EditorPage getOrCreateEditorPage();
    GamePage gamePage();
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    default Vector2i worldSizeInTilesOrElse(Vector2i defaultSize) {
        if (game().level().isEmpty() || game().level().get().world() == null) { return defaultSize; }
        TileMap terrain = game().level().get().world().map().terrain();
        return new Vector2i(terrain.numCols(), terrain.numRows());
    }

    // Actions
    void ifGameActionRun(GameActionProvider actionProvider);
    void ifGameActionRunElse(GameActionProvider actionProvider, Runnable defaultAction);

    // Game scenes
    GameSceneConfig gameSceneConfig(GameVariant variant);
    default GameSceneConfig currentGameSceneConfig() { return gameSceneConfig(gameVariant()); }
    boolean currentGameSceneHasID(String gameSceneID);
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    void togglePlayScene2D3D();
}