/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.PredefinedGameVariant;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    private static final String GAME_VARIANT_NAME = PredefinedGameVariant.PACMAN.name();

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.2 * height;
        ui = GameUI_Builder.createUI(primaryStage, width, height)
            .game(
                GAME_VARIANT_NAME,
                ArcadePacMan_GameModel.class,
                ArcadePacMan_UIConfig.class
            )
            .startPage(ArcadePacMan_StartPage.class, GAME_VARIANT_NAME)
            .dashboard(
                DashboardID.GENERAL, DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO, DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                DashboardID.ABOUT)
            .build();
        ui.showUI();
    }

    @Override
    public void stop() {
        ui.terminate();
    }
}