/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui.dashboard.Dashboard;
import de.amr.games.pacman.ui.input.GameKeyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;

import java.util.Optional;

public interface GameUI {
    enum DashboardID { README, GENERAL, GAME_CONTROL, SETTINGS_3D, GAME_INFO, ACTOR_INFO, KEYBOARD, ABOUT, CUSTOM_MAPS, JOYPAD }
    void addStartPage(StartPage startPage);
    GameAssets assets();
    void boot();
    void build(Stage stage, Dimension2D size);
    void buildDashboard(DashboardID... ids);
    GameClockFX clock();
    UIConfigurationManager configurations();
    Optional<GameScene> currentGameScene();
    View currentView();
    Dashboard dashboard();
    GameKeyboard keyboard();
    void selectGameVariant(GameVariant variant);
    void selectStartPage(int index);
    void show();
    void showEditorView();
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);
    void showGameView();
    void showStartView();
    GameSound sound();
    void updateGameScene(boolean reload);
}