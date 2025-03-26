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
import de.amr.games.pacman.ui.input.Keyboard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;

import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;

public interface GameUI {

    KeyCodeCombination KEY_FULLSCREEN = Keyboard.naked(KeyCode.F11);
    KeyCodeCombination KEY_MUTE = Keyboard.alt(KeyCode.M);
    KeyCodeCombination KEY_OPEN_EDITOR = Keyboard.shift_alt(KeyCode.E);

    ArcadeKeyBinding arcadeKeys();
    JoypadKeyBinding joypadKeyBinding();
    void selectNextJoypadKeyBinding();

    default GameUIConfiguration currentUIConfig() { return gameConfiguration(THE_GAME_CONTROLLER.selectedGameVariant()); }

    Optional<GameScene> currentGameScene();

    default boolean currentGameSceneIsPlayScene2D() {
        return currentGameScene().isPresent()
                && currentUIConfig().gameSceneHasID(currentGameScene().get(), "PlayScene2D");
    }

    default boolean currentGameSceneIsPlayScene3D() {
        return currentGameScene().isPresent()
                && currentUIConfig().gameSceneHasID(currentGameScene().get(), "PlayScene3D");
    }

    void enterFullScreenMode();

    ReadOnlyDoubleProperty heightProperty();

    GameUIConfiguration gameConfiguration(GameVariant variant);

    Pane gameView();

    ObjectProperty<GameScene> gameSceneProperty();

    void init(GameVariant variant);

    boolean isScoreVisible();

    void openEditor();

    ObjectProperty<View> viewProperty();

    void setScoreVisible(boolean visible);

    void showStartView();

    StartPageSelectionView startPageSelectionView();

    void showGameView();

    void showFlashMessageSec(double seconds, String message, Object... args);

    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }

    void togglePlayScene2D3D();

    default Vector2i worldSizeInTilesOrElse(Vector2i defaultSize) {
        if (THE_GAME_CONTROLLER.game().level().isEmpty()) { return defaultSize; }
        WorldMap worldMap = THE_GAME_CONTROLLER.game().level().get().worldMap();
        return new Vector2i(worldMap.numCols(), worldMap.numRows());
    }
}