/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.initGame;
import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.ui.GameUI.theUI;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc. ("all you can play").
 */
public class PacManGames3dApp extends Application {

    @Override
    public void init() {
        initGame();
    }

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 16:10
        long height = Math.round(0.8 * Screen.getPrimary().getBounds().getHeight());
        long width  = Math.round(1.6 * height);
        var mapSelectorXXL = new PacManXXL_Common_MapSelector(theGameContext().theCustomMapDir());
        GameUI.build(theGameContext(), primaryStage, width, height)
            .game(
                GameVariant.PACMAN.name(),
                ArcadePacMan_GameModel.arcadeVersion(theGameContext()),
                ArcadePacMan_UIConfig.class
            )
            .game(
                GameVariant.MS_PACMAN.name(),
                ArcadeMsPacMan_GameModel.arcadeVersion(theGameContext()),
                ArcadeMsPacMan_UIConfig.class
            )
            .game(
                GameVariant.MS_PACMAN_TENGEN.name(),
                new TengenMsPacMan_GameModel(theGameContext()),
                TengenMsPacMan_UIConfig.class
            )
            .game(
                GameVariant.PACMAN_XXL.name(),
                new PacManXXL_PacMan_GameModel(theGameContext(), mapSelectorXXL),
                PacManXXL_PacMan_UIConfig.class
            )
            .game(
                GameVariant.MS_PACMAN_XXL.name(),
                new PacManXXL_MsPacMan_GameModel(theGameContext(), mapSelectorXXL),
                PacManXXL_MsPacMan_UIConfig.class
            )
            .startPages(
                //TODO accessing the "workpiece" while getting constructed is dubious!
                new ArcadePacMan_StartPage(theUI(), GameVariant.PACMAN.name()),
                new ArcadeMsPacMan_StartPage(theUI(), GameVariant.MS_PACMAN.name()),
                new TengenMsPacMan_StartPage(theUI(), GameVariant.MS_PACMAN_TENGEN.name()),
                new PacManXXL_Common_StartPage(theGameContext())
            )
            .dashboardEntries(
                DashboardID.GENERAL, DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.ANIMATION_INFO,
                DashboardID.GAME_INFO, DashboardID.ACTOR_INFO,
                DashboardID.CUSTOM_MAPS,
                DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                DashboardID.ABOUT
            )
            .build()
            .show();

        theUI().theWatchdog().addEventListener(watchEvents -> mapSelectorXXL.loadCustomMaps());
    }

    @Override
    public void stop() {
        theUI().theGameClock().stop();
        theUI().theWatchdog().dispose();
    }
}