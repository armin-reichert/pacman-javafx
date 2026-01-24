/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;

import java.io.File;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

public class PacManXXL_App extends Application {

    private static final double ASPECT_RATIO    = 1.6;
    private static final double HEIGHT_FRACTION = 0.8;

    private static final File HIGH_SCORE_FILE_PACMAN_XXL = GameContext.highScoreFile(GameVariant.ARCADE_PACMAN_XXL);
    private static final File HIGH_SCORE_FILE_MS_PACMAN_XXL = GameContext.highScoreFile(GameVariant.ARCADE_MS_PACMAN_XXL);

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final Dimension2D sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final var mapSelector = new PacManXXL_MapSelector(GameBox.CUSTOM_MAP_DIR);

        ui = GameUI_Builder
            .newUI(primaryStage, sceneSize.getWidth(), sceneSize.getHeight())
            .game(GameVariant.ARCADE_PACMAN_XXL,
                () -> new PacManXXL_PacMan_GameModel(THE_GAME_BOX, mapSelector, HIGH_SCORE_FILE_PACMAN_XXL),
                PacManXXL_PacMan_UIConfig::new)
            .game(GameVariant.ARCADE_MS_PACMAN_XXL,
                () -> new PacManXXL_MsPacMan_GameModel(THE_GAME_BOX, mapSelector, HIGH_SCORE_FILE_MS_PACMAN_XXL),
                PacManXXL_MsPacMan_UIConfig::new)
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
            .startPage(PacManXXL_StartPage::new)
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