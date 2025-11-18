/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.app;

import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.model.StandardGameVariant.MS_PACMAN_TENGEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_ASPECT;

public class TengenMsPacMan_App extends Application {

    private static final String GAME_VARIANT_NAME = MS_PACMAN_TENGEN.name();

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect NES screen aspect 32:30
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = NES_ASPECT * height;
        ui = GameUI_Builder.createUI(primaryStage, width, height)
            .game(
                GAME_VARIANT_NAME,
                TengenMsPacMan_GameModel.class,
                TengenMsPacMan_UIConfig.class
            )
            .startPage(TengenMsPacMan_StartPage.class, GAME_VARIANT_NAME)
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