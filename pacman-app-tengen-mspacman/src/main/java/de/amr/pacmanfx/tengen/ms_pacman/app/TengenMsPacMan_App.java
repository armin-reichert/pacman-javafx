/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.app;

import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.initGame;
import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_ASPECT;
import static de.amr.pacmanfx.ui.GameUI.theUI;
import static de.amr.pacmanfx.ui.GameVariant.MS_PACMAN_TENGEN;

public class TengenMsPacMan_App extends Application {

    @Override
    public void init() {
        initGame();
    }

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect NES screen aspect 32:30
        final double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        final double width  = NES_ASPECT * height;
        GameUI.build(theGameContext(), primaryStage, width, height)
            .game(
                MS_PACMAN_TENGEN.name(),
                new TengenMsPacMan_GameModel(theGameContext()),
                TengenMsPacMan_UIConfig.class
            )
            .startPages(
                new TengenMsPacMan_StartPage(theUI(), MS_PACMAN_TENGEN.name())
            )
            .dashboardEntries(
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
        theUI().theGameClock().stop();
        theUI().theWatchdog().dispose();
    }
}