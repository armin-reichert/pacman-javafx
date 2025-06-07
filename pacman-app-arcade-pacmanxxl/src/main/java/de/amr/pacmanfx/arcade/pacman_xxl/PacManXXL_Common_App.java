/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.PacManGames_Env;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames_Env.initUI;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

public class PacManXXL_Common_App extends Application {

    private PacManXXL_Common_MapSelector xxlMapSelector;

    @Override
    public void init() {
        PacManGames_Env.init();
        xxlMapSelector = new PacManXXL_Common_MapSelector(CUSTOM_MAP_DIR);
        theGameController().registerGame(GameVariant.PACMAN_XXL, new PacManXXL_PacMan_GameModel(xxlMapSelector));
        theGameController().registerGame(GameVariant.MS_PACMAN_XXL, new PacManXXL_MsPacMan_GameModel(xxlMapSelector));
        theGameController().selectGameVariant(GameVariant.MS_PACMAN_XXL);
    }

    @Override
    public void start(Stage stage) {
        initUI(Map.of(
            GameVariant.PACMAN_XXL,    PacManXXL_PacMan_UIConfig.class,
            GameVariant.MS_PACMAN_XXL, PacManXXL_MsPacMan_UIConfig.class)
        );

        // UI size: 80% of available screen height, aspect as screen
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        theUI().buildUI(stage, aspect * height, height,
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

        theUI().addStartPage(new PacManXXL_Common_StartPage());
        theUI().selectStartPage(0);

        theUI().show();
    }
}