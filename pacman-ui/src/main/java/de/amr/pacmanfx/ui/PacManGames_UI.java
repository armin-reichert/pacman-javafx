/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.GameView;
import de.amr.pacmanfx.ui.layout.PacManGames_View;
import de.amr.pacmanfx.ui.layout.StartPagesView;
import de.amr.pacmanfx.uilib.GameScene;
import javafx.beans.property.ObjectProperty;
import javafx.stage.Stage;

import java.util.Optional;

public interface PacManGames_UI {

    void buildUI(Stage stage, double width, double height, DashboardID... dashboardIDs);
    void restart();
    void selectGameVariant(GameVariant variant);
    void show();

    // Configuration
    PacManGames_UIConfiguration configuration(GameVariant gameVariant);
    PacManGames_UIConfiguration configuration();
    void setConfiguration(GameVariant variant, PacManGames_UIConfiguration configuration);

    // Game scenes
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();
    boolean currentGameSceneIsPlayScene2D();
    boolean currentGameSceneIsPlayScene3D();
    boolean currentGameSceneIs2D();
    void updateGameScene(boolean reload);

    // Views
    PacManGames_View currentView();
    GameView gameView();
    StartPagesView startPagesView();
    void showEditorView();
    void showGameView();
    void showStartView();

    // Flash messages
    default void showFlashMessage(String message, Object... args) { showFlashMessageSec(1.5, message, args); }
    void showFlashMessageSec(double seconds, String message, Object... args);
}