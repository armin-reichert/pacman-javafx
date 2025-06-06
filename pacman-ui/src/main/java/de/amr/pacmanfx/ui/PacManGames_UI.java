/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.GameScene;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Optional;

public interface PacManGames_UI {
    void buildUI(Stage stage, double width, double height, DashboardID... dashboardIDs);
    PacManGames_UIConfiguration configuration(GameVariant gameVariant);
    PacManGames_UIConfiguration currentConfig();
    Optional<GameScene> currentGameScene();
    boolean currentGameSceneIsPlayScene2D();
    boolean currentGameSceneIsPlayScene3D();
    boolean currentGameSceneIs2D();
    PacManGames_View currentView();
    ObjectProperty<GameScene> gameSceneProperty();
    GameView gameView();
    void restart();
    void selectGameVariant(GameVariant variant);
    void setConfig(GameVariant variant, PacManGames_UIConfiguration uiConfig);
    void show();
    void showEditorView();
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1.5, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);
    void showGameView();
    void showStartView();
    StartPagesView startPagesView();
    void updateGameScene(boolean reload);
}