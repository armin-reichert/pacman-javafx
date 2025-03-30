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
    void addStartPage(GameVariant gameVariant, StartPage startPage);
    void addDefaultDashboardItems(String... ids);
    GameAssets assets();
    void build(Stage stage, Dimension2D size);
    GameClockFX clock();
    UIConfigurationManager configurations();
    View currentView();
    Optional<GameScene> currentGameScene();
    Dashboard dashboard();
    void enterFullScreenMode();
    ObjectProperty<GameScene> gameSceneProperty();
    GameKeyboard keyboard();
    void init(GameVariant variant);
    boolean isScoreVisible();
    void onGameVariantChange(GameVariant gameVariant);
    void setScoreVisible(boolean visible);
    void show();
    void showEditorView();
    void showGameView();
    void showStartView();
    void showFlashMessageSec(double seconds, String message, Object... args);
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1, message, args); }
    GameSound sound();
    void togglePlayScene2D3D();
    void updateGameScene(boolean reload);
}