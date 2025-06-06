/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.PacManGames_Env;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;

/**
 * Application containing all game variants and including 3D play scene.
 *
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    @Override
    public void init() {
        PacManGames_Env.init();
        var xxlMapSelector = new PacManXXL_Common_MapSelector(CUSTOM_MAP_DIR);
        theGameController().registerGame(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel());
        theGameController().registerGame(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
        theGameController().registerGame(GameVariant.PACMAN, new ArcadePacMan_GameModel());
        theGameController().registerGame(GameVariant.PACMAN_XXL, new PacManXXL_PacMan_GameModel(xxlMapSelector));
        theGameController().registerGame(GameVariant.MS_PACMAN_XXL, new PacManXXL_MsPacMan_GameModel(xxlMapSelector));
        theGameController().selectGameVariant(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        initUI(Map.of(
            GameVariant.PACMAN,           ArcadePacMan_UIConfig.class,
            GameVariant.MS_PACMAN,        ArcadeMsPacMan_UIConfig.class,
            GameVariant.MS_PACMAN_TENGEN, TengenMsPacMan_UIConfig.class,
            GameVariant.PACMAN_XXL,       PacManXXL_PacMan_UIConfig.class,
            GameVariant.MS_PACMAN_XXL,    PacManXXL_MsPacMan_UIConfig.class
        ));

        // UI size: 80% of available screen height, aspect 16:10
        double height = 0.8 * Screen.getPrimary().getBounds().getHeight();
        theUI().buildUI(stage, 1.6 * height, height,
            DashboardID.README,
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.CUSTOM_MAPS,
            DashboardID.KEYBOARD,
            DashboardID.ABOUT);

        InfoBoxCustomMaps infoBoxCustomMaps = theUI().gameView().dashboard().getInfoBox(DashboardID.CUSTOM_MAPS);
        var mapSelector = (PacManXXL_Common_MapSelector) theGameController().game(GameVariant.PACMAN_XXL).mapSelector();
        infoBoxCustomMaps.setTableItems(mapSelector.customMaps());
        mapSelector.startWatchingCustomMaps();

        theUI().startPagesView().addStartPage(new ArcadePacMan_StartPage(GameVariant.PACMAN));
        theUI().startPagesView().addStartPage(new ArcadeMsPacMan_StartPage(GameVariant.MS_PACMAN));
        theUI().startPagesView().addStartPage(new TengenMsPacMan_StartPage(GameVariant.MS_PACMAN_TENGEN));
        theUI().startPagesView().addStartPage(new PacManXXL_Common_StartPage());
        theUI().startPagesView().selectStartPage(0);

        theUI().show();
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}