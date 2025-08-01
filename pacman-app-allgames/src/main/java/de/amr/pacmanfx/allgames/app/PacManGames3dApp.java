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
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.ui.GameUI.theUI;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc. ("all you can f*** ähm play").
 */
public class PacManGames3dApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Use 80% of available screen height, aspect 16:10
        int height = (int) Math.round(0.8 * Screen.getPrimary().getBounds().getHeight());
        int width  = (int) Math.round(1.6 * height);

        //TODO create this too by reflection inside builder?
        var mapSelectorXXL = new PacManXXL_Common_MapSelector(theGameContext().theCustomMapDir());

        GameUI_Builder.createUI(primaryStage, width, height)
            .game(GameVariant.PACMAN.name(),
                ArcadePacMan_GameModel.class, ArcadePacMan_UIConfig.class)
            .game(GameVariant.MS_PACMAN.name(),
                ArcadeMsPacMan_GameModel.class, ArcadeMsPacMan_UIConfig.class)
            .game(GameVariant.MS_PACMAN_TENGEN.name(),
                TengenMsPacMan_GameModel.class, TengenMsPacMan_UIConfig.class)
            .game(GameVariant.PACMAN_XXL.name(),
                PacManXXL_PacMan_GameModel.class, mapSelectorXXL, PacManXXL_PacMan_UIConfig.class)
            .game(GameVariant.MS_PACMAN_XXL.name(),
                PacManXXL_MsPacMan_GameModel.class, mapSelectorXXL, PacManXXL_MsPacMan_UIConfig.class)
            // start pages are added to carousel in this order:
            .startPage(GameVariant.PACMAN.name(), ArcadePacMan_StartPage.class)
            .startPage(GameVariant.MS_PACMAN.name(), ArcadeMsPacMan_StartPage.class)
            .startPage(GameVariant.MS_PACMAN_TENGEN.name(), TengenMsPacMan_StartPage.class)
            .startPage(GameVariant.PACMAN_XXL.name(), PacManXXL_Common_StartPage.class) // MS_PACMAN_XXL uses this too
            .dashboard(
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

        theUI().theCustomDirWatchdog().addEventListener(watchEvents -> mapSelectorXXL.loadCustomMaps());
    }

    @Override
    public void stop() {
        theUI().terminate();
    }
}