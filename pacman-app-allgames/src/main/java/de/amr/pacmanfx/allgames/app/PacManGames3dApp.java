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
import de.amr.pacmanfx.ui.PacManGamesEnv;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

/**
 * Application containing all game variants and including 3D play scene.
 *
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private XXLAnyPacMan_MapSelector xxlMapSelector;

    @Override
    public void init() {
        try {
            PacManGamesEnv.init();
            xxlMapSelector = new XXLAnyPacMan_MapSelector(CUSTOM_MAP_DIR);
            theGameController().register(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel());
            theGameController().register(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
            theGameController().register(GameVariant.PACMAN, new ArcadePacMan_GameModel());
            theGameController().register(GameVariant.PACMAN_XXL, new XXLPacMan_GameModel(xxlMapSelector));
            theGameController().register(GameVariant.MS_PACMAN_XXL, new XXLMsPacMan_GameModel(xxlMapSelector));
            theGameController().select(GameVariant.PACMAN);
        } catch (Exception x) {
            Logger.error(x);
        }
    }

    @Override
    public void start(Stage stage) {
        createUI(Map.of(
            GameVariant.PACMAN,           ArcadePacMan_UIConfig.class,
            GameVariant.MS_PACMAN,        ArcadeMsPacMan_UIConfig.class,
            GameVariant.MS_PACMAN_TENGEN, TengenMsPacMan_UIConfig.class,
            GameVariant.PACMAN_XXL,       XXLPacMan_UIConfig.class,
            GameVariant.MS_PACMAN_XXL,    XXLMsPacMan_UIConfig.class
        ));

        // UI size: 80% of available screen height, aspect 16:10
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.6 * height;
        theUI().build(stage, width, height);

        theUI().buildDashboard(
                DashboardID.README,
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.CUSTOM_MAPS,
                DashboardID.KEYBOARD,
                DashboardID.ABOUT);

        InfoBoxCustomMaps infoBoxCustomMaps = theUI().dashboard().getInfoBox(DashboardID.CUSTOM_MAPS);
        infoBoxCustomMaps.setTableItems(xxlMapSelector.customMaps());
        xxlMapSelector.startWatchingCustomMaps();

        theUI().addStartPage(new ArcadePacMan_StartPage(GameVariant.PACMAN));
        theUI().addStartPage(new ArcadeMsPacMan_StartPage(GameVariant.MS_PACMAN));
        theUI().addStartPage(new TengenMsPacMan_StartPage(GameVariant.MS_PACMAN_TENGEN));
        theUI().addStartPage(new XXLAnyPacMan_StartPage());
        theUI().selectStartPage(0);

        theUI().show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        theClock().stop();
    }
}