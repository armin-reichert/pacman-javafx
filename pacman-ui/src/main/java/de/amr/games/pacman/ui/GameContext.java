/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.StartPageSelectionView;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Assets
    AssetStorage assets();
    GameSound sound();

    default String locText(String keyOrPattern, Object... args) {
        assertNotNull(keyOrPattern);
        for (var bundle : assets().bundles()) {
            if (bundle.containsKey(keyOrPattern)) {
                return MessageFormat.format(bundle.getString(keyOrPattern), args);
            }
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return "[" + keyOrPattern + "]";
    }

    String localizedGameOverMessage();
    String localizedLevelCompleteMessage(int levelNumber);

    // Game model and controller
    GameClockFX gameClock();
    default GameState gameState() { return THE_GAME_CONTROLLER.state(); }
    default <GAME extends GameModel> GAME game() { return THE_GAME_CONTROLLER.game(); }
    GameVariant gameVariant();
    void setGameVariant(GameVariant variant);

    // Input
    Keyboard keyboard();
    ArcadeKeyBinding arcadeKeys();
    JoypadKeyBinding joypadKeyBinding();
    void selectNextJoypadKeyBinding();

    // GUI
    ReadOnlyDoubleProperty heightProperty();
    GameUIConfiguration gameConfiguration(GameVariant variant);
    default GameUIConfiguration gameConfiguration() { return gameConfiguration(gameVariant()); }
    void setScoreVisible(boolean visible);
    boolean isScoreVisible();
    ObjectProperty<Node> viewProperty();
    void showStartView();
    StartPageSelectionView startPageSelectionView();
    void showGameView();
    Pane gameView();
    void openEditor();
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    default Vector2i worldSizeInTilesOrElse(Vector2i defaultSize) {
        if (game().level().isEmpty()) { return defaultSize; }
        WorldMap worldMap = game().level().get().worldMap();
        return new Vector2i(worldMap.numCols(), worldMap.numRows());
    }

    // Game scenes

    default boolean currentGameSceneHasID(String gameSceneID) {
        assertNotNull(gameSceneID);
        return currentGameScene().isPresent()
            && gameConfiguration().gameSceneHasID(currentGameScene().get(), gameSceneID);
    }

    ObjectProperty<GameScene> gameSceneProperty();

    Optional<GameScene> currentGameScene();

    void togglePlayScene2D3D();
}