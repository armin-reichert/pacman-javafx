/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.action.GameActionProvider;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.assets.GameSound;
import de.amr.games.pacman.ui.input.ArcadeInput;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.lib.GameClockFX;
import de.amr.games.pacman.ui.scene.GameConfiguration;
import de.amr.games.pacman.ui.scene.GameScene;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Assets
    AssetStorage assets();
    GameSound sound();

    default String locText(String keyOrPattern, Object... args) {
        checkNotNull(keyOrPattern);
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
    ArcadeInput arcadeKeys();
    JoypadKeyBinding joypadKeys();
    void enableJoypad();
    void disableJoypad();
    void nextJoypadKeys();

    // GUI
    void setScoreVisible(boolean visible);
    boolean isScoreVisible();
    void selectPage(Node page);
    void selectStartPage();
    void selectGamePage();
    Pane gamePage();
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    default Vector2i worldSizeInTilesOrElse(Vector2i defaultSize) {
        if (game().level().isEmpty() || game().level().get().world() == null) { return defaultSize; }
        TileMap terrain = game().level().get().world().map().terrain();
        return new Vector2i(terrain.numCols(), terrain.numRows());
    }

    // Actions
    default void ifGameActionTriggeredRunIt(GameActionProvider actionProvider) {
        actionProvider.firstMatchedAction(keyboard())
            .filter(gameAction -> gameAction.isEnabled(this))
            .ifPresent(action -> action.execute(this));
    }

    default void ifGameActionTriggeredRunItElse(GameActionProvider actionProvider, Runnable defaultAction) {
        actionProvider.firstMatchedAction(keyboard())
            .filter(gameAction -> gameAction.isEnabled(this))
            .ifPresentOrElse(action -> action.execute(this), defaultAction);
    }

    // Game scenes
    GameConfiguration gameSceneConfig(GameVariant variant);
    default GameConfiguration currentGameConfig() { return gameSceneConfig(gameVariant()); }
    boolean currentGameSceneHasID(String gameSceneID);
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    void togglePlayScene2D3D();
}