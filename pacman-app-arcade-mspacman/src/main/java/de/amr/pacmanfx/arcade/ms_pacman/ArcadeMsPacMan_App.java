/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.ui.GameUI.theUI;

public class ArcadeMsPacMan_App extends Application {

    private static final String GAME_VARIANT = GameVariant.MS_PACMAN.name();

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 12:10
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = 1.2 * height;
        GameUI_Builder.createUI(primaryStage, width, height)
            .game(
                GAME_VARIANT,
                ArcadeMsPacMan_GameModel.class,
                ArcadeMsPacMan_UIConfig.class
            )
            .startPage(GAME_VARIANT, ArcadeMsPacMan_StartPage.class)
            .dashboard(
                DashboardID.GENERAL, DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO, DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                DashboardID.ABOUT
            )
        .build()
        .show();
    }

    @Override
    public void stop() {
        theUI().terminate();
    }
}