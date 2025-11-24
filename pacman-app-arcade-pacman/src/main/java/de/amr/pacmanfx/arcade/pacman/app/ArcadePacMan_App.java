/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    private static final String GAME_VARIANT_NAME = StandardGameVariant.PACMAN.name();

    private static final float ASPECT_RATIO = 1.2f; // 12:10 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getBounds().getHeight());
        final int width  = Math.round(ASPECT_RATIO * height);

        ui = GameUI_Builder.create(primaryStage, width, height)
            .game(
                GAME_VARIANT_NAME,
                ArcadePacMan_GameModel.class,
                ArcadePacMan_UIConfig.class
            )
            .startPage(ArcadePacMan_StartPage.class, GAME_VARIANT_NAME)
            .dashboard(
                DashboardID.GENERAL, DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO, DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD_SHORTCUTS_GLOBAL, DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                DashboardID.ABOUT)
            .build();
        ui.showUI();
    }

    @Override
    public void stop() {
        ui.terminate();
    }
}