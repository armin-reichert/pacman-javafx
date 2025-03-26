/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.GameView;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._2d.StartPagesCarousel;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.UIGlobals.THE_ASSETS;
import static de.amr.games.pacman.ui.UIGlobals.THE_UI;

public interface GameUI {

    static void create(Map<GameVariant, GameUIConfiguration> configMap, boolean support3D) {
        THE_UI = support3D ? new PacManGamesUI_3D() : new PacManGamesUI();
        if (support3D) {
            THE_ASSETS.addAssets3D();
        }
        for (var entry : configMap.entrySet()) {
            THE_UI.configure(entry.getKey(), entry.getValue());
        }
    }


    KeyCodeCombination KEY_FULLSCREEN = Keyboard.naked(KeyCode.F11);
    KeyCodeCombination KEY_MUTE = Keyboard.alt(KeyCode.M);
    KeyCodeCombination KEY_OPEN_EDITOR = Keyboard.shift_alt(KeyCode.E);

    ArcadeKeyBinding arcadeKeys();
    JoypadKeyBinding joypadKeyBinding();
    void selectNextJoypadKeyBinding();

    void addStartPage(GameVariant gameVariant, StartPage startPage);

    void addDefaultDashboardItems(String... titles);

    void configure(GameVariant gameVariant, GameUIConfiguration configuration);

    void create(Stage stage, Dimension2D size);

    default GameUIConfiguration currentUIConfig() { return uiConfiguration(THE_GAME_CONTROLLER.selectedGameVariant()); }

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

    GameUIConfiguration uiConfiguration(GameVariant variant);

    GameView gameView();

    ObjectProperty<GameScene> gameSceneProperty();

    void init(GameVariant variant);

    boolean isScoreVisible();

    void openEditor();

    void setScoreVisible(boolean visible);

    void show();

    void showStartView();

    void showGameView();

    void showFlashMessageSec(double seconds, String message, Object... args);

    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }

    StartPagesCarousel startPageSelectionView();

    void togglePlayScene2D3D();
}