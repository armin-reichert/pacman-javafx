/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.property.ObjectProperty;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Assets
    AssetStorage assets();
    GameSound sound();

    default String locText(String keyOrPattern, Object... args) {
        Globals.assertNotNull(keyOrPattern);
        for (var bundle : assets().bundles()) {
            if (bundle.containsKey(keyOrPattern)) {
                return MessageFormat.format(bundle.getString(keyOrPattern), args);
            }
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }

    String locGameOverMessage();
    String locLevelCompleteMessage(int levelNumber);

    // Game model and controller

    GameClockFX gameClock();

    default GameController gameController() { return GameController.it(); }

    default GameState gameState() { return gameController().state(); }

    @SuppressWarnings("unchecked")
    default <GAME extends GameModel> GAME game() { return (GAME) gameController().currentGame(); }

    /** @return the current game level, throw exception if level does not exist */
    default GameLevel level() { return game().level().orElseThrow(); }

    GameVariant gameVariant();

    void selectGameVariant(GameVariant variant);

    default long tick() { return gameClock().getTickCount(); }

    // Input
    Keyboard keyboard();
    ArcadeKeyBinding arcadeKeys();
    JoypadKeyBinding currentJoypadKeyBinding();
    void registerJoypadKeyBinding();
    void unregisterJoypadKeyBinding();
    void selectNextJoypadKeyBinding();

    // GUI
    GameUIConfiguration gameConfiguration(GameVariant variant);
    default GameUIConfiguration gameConfiguration() { return gameConfiguration(gameVariant()); }
    void setScoreVisible(boolean visible);
    boolean isScoreVisible();
    void showStartView();
    void showGameView();
    Pane gameView();
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    default Vector2i worldSizeInTilesOrElse(Vector2i defaultSize) {
        if (game().level().isEmpty()) { return defaultSize; }
        WorldMap worldMap = game().level().get().map();
        return new Vector2i(worldMap.numCols(), worldMap.numRows());
    }

    // Actions
    default void ifTriggeredRunAction(GameActionProvider actionProvider) {
        actionProvider.firstMatchedAction(keyboard())
            .filter(gameAction -> gameAction.isEnabled(this))
            .ifPresent(action -> action.execute(this));
    }

    default void ifTriggeredRunActionElse(GameActionProvider actionProvider, Runnable defaultAction) {
        actionProvider.firstMatchedAction(keyboard())
            .filter(gameAction -> gameAction.isEnabled(this))
            .ifPresentOrElse(action -> action.execute(this), defaultAction);
    }

    // Game scenes
    boolean currentGameSceneHasID(String gameSceneID);
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    void togglePlayScene2D3D();
}