/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.DashboardID;
import de.amr.games.pacman.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.Map;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui.Globals.createUI;

public class PacManXXL_App extends Application {

    private PacManXXL_MapSelector xxlMapSelector;

    @Override
    public void init() {
        checkUserDirsExistAndWritable();
        xxlMapSelector = new PacManXXL_MapSelector(CUSTOM_MAP_DIR);
        var pacManGameModel = new PacManXXL_PacMan_GameModel(xxlMapSelector);
        var msPacManGameModel = new PacManXXL_MsPacMan_GameModel(xxlMapSelector);
        THE_GAME_CONTROLLER.register(GameVariant.PACMAN_XXL, pacManGameModel);
        THE_GAME_CONTROLLER.register(GameVariant.MS_PACMAN_XXL, msPacManGameModel);
        THE_GAME_CONTROLLER.select(GameVariant.MS_PACMAN_XXL);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight(), width = aspect * height;
        createUI(Map.of(
            GameVariant.PACMAN_XXL,    PacManXXL_PacMan_UIConfig.class,
            GameVariant.MS_PACMAN_XXL, PacManXXL_MsPacMan_UIConfig.class)
        );
        THE_UI.build(stage, width, height);
        THE_UI.buildDashboard(
                DashboardID.README,
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.CUSTOM_MAPS,
                DashboardID.KEYBOARD,
                DashboardID.ABOUT);

        InfoBoxCustomMaps infoBoxCustomMaps = THE_UI.dashboard().getInfoBox(DashboardID.CUSTOM_MAPS);
        infoBoxCustomMaps.setTableItems(xxlMapSelector.customMaps());

        THE_UI.addStartPage(new PacManXXL_StartPage());
        THE_UI.selectStartPage(0);
        THE_UI.show();
    }
}