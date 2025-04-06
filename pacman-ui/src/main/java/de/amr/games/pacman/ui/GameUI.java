/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.dashboard.Dashboard;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Optional;

public interface GameUI {
    enum DashboardID { README, GENERAL, GAME_CONTROL, SETTINGS_3D, GAME_INFO, ACTOR_INFO, KEYBOARD, ABOUT, CUSTOM_MAPS, JOYPAD }

    void addStartPage(StartPage startPage);
    void build(Stage stage, Dimension2D size);
    void buildDashboard(DashboardID... ids);
    GameClockFX clock();
    GameUIConfigManager gameUIConfigManager();
    Optional<GameScene> currentGameScene();
    View currentView();
    Dashboard dashboard();
    ObjectProperty<GameScene> gameSceneProperty();
    GameKeyboard keyboard();
    Scene mainScene();
    void restart();
    void selectGameVariant(GameVariant variant);
    void selectStartPage(int index);
    void show();
    void showEditorView();
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1.5, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);
    void showGameView();
    void showStartView();
    void updateGameScene(boolean reload);
}