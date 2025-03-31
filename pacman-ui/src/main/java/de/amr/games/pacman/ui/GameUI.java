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
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;

import java.util.Optional;

public interface GameUI {
    enum DashboardID { README, GENERAL, GAME_CONTROL, SETTINGS_3D, GAME_INFO, ACTOR_INFO, KEYBOARD, ABOUT, CUSTOM_MAPS, JOYPAD }
    void addStartPage(StartPage startPage);
    void addDefaultDashboardItems(DashboardID... ids);
    GameAssets assets();
    void build(Stage stage, Dimension2D size);
    GameClockFX clock();
    UIConfigurationManager configurations();
    View currentView();
    Optional<GameScene> currentGameScene();
    Dashboard dashboard();
    GameKeyboard keyboard();
    void selectGameVariant(GameVariant variant);
    void onGameVariantChange(GameVariant gameVariant);
    void selectStartPage(int index);
    void show();
    void showEditorView();
    void showGameView();
    void showStartView();
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    GameSound sound();
    void updateGameScene(boolean reload);
}