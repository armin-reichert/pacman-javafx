/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.model.StandardGameVariant.MS_PACMAN_XXL;
import static de.amr.pacmanfx.model.StandardGameVariant.PACMAN_XXL;

public class PacManXXL_App extends Application {

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        // UI size: 80% of available screen height, aspect 16:10
        final double height = 0.8 * Screen.getPrimary().getVisualBounds().getHeight();
        final double width  = 1.6 * height;
        final var mapSelector = new PacManXXL_MapSelector(GameBox.CUSTOM_MAP_DIR);
        ui = GameUI_Builder.create(primaryStage, width, height)
                .game(
                    PACMAN_XXL.name(),
                    PacManXXL_PacMan_GameModel.class,
                    mapSelector,
                    PacManXXL_PacMan_UIConfig.class
                )
                .game(
                    MS_PACMAN_XXL.name(),
                    PacManXXL_MsPacMan_GameModel.class,
                    mapSelector,
                    PacManXXL_MsPacMan_UIConfig.class
                )
                .dashboard(
                    DashboardID.README, DashboardID.GENERAL,
                    DashboardID.GAME_CONTROL, DashboardID.SETTINGS_3D,
                    DashboardID.GAME_INFO, DashboardID.ACTOR_INFO, DashboardID.CUSTOM_MAPS,
                    DashboardID.KEYS_GLOBAL, DashboardID.KEYS_LOCAL,
                    DashboardID.ABOUT)
            .startPage(PacManXXL_StartPage.class, PACMAN_XXL.name())
            .build();

        ui.customDirWatchdog().addEventListener(mapSelector);
        ui.selectGameVariant(PACMAN_XXL.name());
        ui.show();
    }

    @Override
    public void stop() {
        ui.terminate();
    }
}