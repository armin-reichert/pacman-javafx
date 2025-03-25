/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.StartPageSelectionView;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Game model and controller
    GameVariant gameVariant();
    void setGameVariant(GameVariant variant);

    // Input
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
        if (THE_GAME_CONTROLLER.game().level().isEmpty()) { return defaultSize; }
        WorldMap worldMap = THE_GAME_CONTROLLER.game().level().get().worldMap();
        return new Vector2i(worldMap.numCols(), worldMap.numRows());
    }

    String localizedGameOverMessage();
    String localizedLevelCompleteMessage(int levelNumber);

    // Game scenes
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    default boolean currentGameSceneIsPlayScene2D() {
        return currentGameScene().isPresent() && gameConfiguration().gameSceneHasID(currentGameScene().get(), "PlayScene2D");
    }
    default boolean currentGameSceneIsPlayScene3D() {
        return currentGameScene().isPresent() && gameConfiguration().gameSceneHasID(currentGameScene().get(), "PlayScene3D");
    }
    void togglePlayScene2D3D();
}