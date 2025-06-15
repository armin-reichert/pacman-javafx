/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.ArcadePacMan_MapSelector;
import de.amr.pacmanfx.arcade.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_MapSelector;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.PacManGames_UIBuilder;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.ui.PacManGames_Env.theClock;
import static de.amr.pacmanfx.ui.PacManGames_UIBuilder.buildUI;

/**
 * Application containing all game variants and including 3D play scene.
 *
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private static final String MS_PACMAN = "MS_PACMAN";
    private static final String MS_PACMAN_TENGEN = "MS_PACMAN_TENGEN";
    private static final String MS_PACMAN_XXL = "MS_PACMAN_XXL";
    private static final String PACMAN = "PACMAN";
    private static final String PACMAN_XXL = "PACMAN_XXL";

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 16:10
        final int height = (int) (0.8 * Screen.getPrimary().getBounds().getHeight());
        final int width  = (int) (1.6 * height);
        final var xxlMapSelector = new PacManXXL_Common_MapSelector(CUSTOM_MAP_DIR);
        buildUI()
            .stage(primaryStage, width, height)
            .startPage(             new ArcadePacMan_StartPage(PACMAN))
            .game(PACMAN,           new ArcadePacMan_GameModel(new ArcadePacMan_MapSelector()), ArcadePacMan_UIConfig.class)
            .startPage(             new ArcadeMsPacMan_StartPage(MS_PACMAN))
            .game(MS_PACMAN,        new ArcadeMsPacMan_GameModel(new ArcadeMsPacMan_MapSelector()), ArcadeMsPacMan_UIConfig.class)
            .startPage(             new TengenMsPacMan_StartPage(MS_PACMAN_TENGEN))
            .game(MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel(), TengenMsPacMan_UIConfig.class)
            .startPage(             new PacManXXL_Common_StartPage())
            .game(PACMAN_XXL,       new PacManXXL_PacMan_GameModel(xxlMapSelector),   PacManXXL_PacMan_UIConfig.class)
            .game(MS_PACMAN_XXL,    new PacManXXL_MsPacMan_GameModel(xxlMapSelector), PacManXXL_MsPacMan_UIConfig.class)
            .dashboardEntries(
                    DashboardID.README,
                    DashboardID.GENERAL,
                    DashboardID.GAME_CONTROL,
                    DashboardID.SETTINGS_3D,
                    DashboardID.GAME_INFO,
                    DashboardID.ACTOR_INFO,
                    DashboardID.CUSTOM_MAPS,
                    DashboardID.KEYBOARD,
                    DashboardID.ABOUT)
            .selectGame(PACMAN)
            .show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}