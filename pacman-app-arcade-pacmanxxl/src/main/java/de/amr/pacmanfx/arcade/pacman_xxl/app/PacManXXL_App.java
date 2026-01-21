/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardSectionCustomMaps;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    public static final double ASPECT_RATIO    = 1.6;
    public static final double HEIGHT_FRACTION = 0.8;

    public static final String PACMAN_GAME   = GameVariant.ARCADE_PACMAN_XXL.name();
    public static final String MSPACMAN_GAME = GameVariant.ARCADE_MS_PACMAN_XXL.name();

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final double availableHeight = Screen.getPrimary().getVisualBounds().getHeight();
        final double height = Math.floor(HEIGHT_FRACTION * availableHeight);
        final double width  = Math.floor(ASPECT_RATIO * height);
        final var mapSelector = new PacManXXL_MapSelector(GameBox.CUSTOM_MAP_DIR);

        ui = GameUI_Builder
            .create(primaryStage, width, height)
            .game(PACMAN_GAME, PacManXXL_PacMan_GameModel.class, mapSelector, PacManXXL_PacMan_UIConfig.class)
            .game(MSPACMAN_GAME, PacManXXL_MsPacMan_GameModel.class, mapSelector, PacManXXL_MsPacMan_UIConfig.class)
            .dashboard(
                CommonDashboardID.README,
                CommonDashboardID.GENERAL,
                CommonDashboardID.GAME_CONTROL,
                CommonDashboardID.SETTINGS_3D,
                CommonDashboardID.GAME_INFO,
                CommonDashboardID.ACTOR_INFO,
                CommonDashboardID.CUSTOM_MAPS,
                CommonDashboardID.KEYS_GLOBAL,
                CommonDashboardID.KEYS_LOCAL,
                CommonDashboardID.ABOUT)
            .startPage(PacManXXL_StartPage.class, PACMAN_GAME)
            .build();

        final Dashboard dashboard = ui.views().playView().dashboard();
        dashboard.findSection(CommonDashboardID.CUSTOM_MAPS)
            .filter(DashboardSectionCustomMaps.class::isInstance)
            .map(DashboardSectionCustomMaps.class::cast)
            .ifPresent(section -> {
                section.setCustomDirWatchDog(ui.customDirWatchdog());
                section.setMapEditFunction(mapFile -> ui.editWorldMap(mapFile));
            });

        ui.customDirWatchdog().addEventListener(mapSelector);
        ui.show();
    }

    @Override
    public void stop() {
        ui.terminate();
    }
}