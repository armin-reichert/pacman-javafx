/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.GameView;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;
import de.amr.games.pacman.uilib.Keyboard;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.stage.Stage;

import java.util.Optional;

public interface GameUI {

    GameClockFX clock();
    GameKeyboard keyboard();
    GameAssets assets();
    GameSound sound();

    void addStartPage(GameVariant gameVariant, StartPage startPage);

    void addDefaultDashboardItems(String... titles);

    void build(Stage stage, Dimension2D size);

    UIConfigurationManager configurations();

    Optional<GameScene> currentGameScene();

    void enterFullScreenMode();

    ReadOnlyDoubleProperty heightProperty();

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

    void togglePlayScene2D3D();

    void updateGameScene(boolean reload);
}