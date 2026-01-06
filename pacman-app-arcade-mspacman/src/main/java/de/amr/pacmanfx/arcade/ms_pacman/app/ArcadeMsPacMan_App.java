/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    private static final String GAME_VARIANT = StandardGameVariant.ARCADE_MS_PACMAN.name();

    private static final float ASPECT_RATIO = 1.2f; // 12:10 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getVisualBounds().getHeight());
        final int width  = Math.round(ASPECT_RATIO * height);

        ui = GameUI_Builder.create(primaryStage, width, height)
            .game(
                GAME_VARIANT,
                ArcadeMsPacMan_GameModel.class,
                ArcadeMsPacMan_UIConfig.class
            )
            .startPage(ArcadeMsPacMan_StartPage.class, GAME_VARIANT)
            .dashboard(
                CommonDashboardID.GENERAL,
                CommonDashboardID.GAME_CONTROL,
                CommonDashboardID.SETTINGS_3D,
                CommonDashboardID.GAME_INFO,
                CommonDashboardID.ACTOR_INFO,
                CommonDashboardID.KEYS_GLOBAL,
                CommonDashboardID.KEYS_LOCAL,
                CommonDashboardID.ABOUT)
            .build();

        ui.show();
    }

    @Override
    public void stop() {
        if (ui != null) {
            ui.terminate();
        }
    }
}