/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    private static final String GAME_VARIANT = StandardGameVariant.MS_PACMAN.name();

    private static final float ASPECT_RATIO = 1.2f; // 12:10 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getBounds().getHeight());
        final int width  = Math.round(ASPECT_RATIO * height);

        ui = GameUI_Builder.createUI(primaryStage, width, height)
            .game(
                GAME_VARIANT,
                ArcadeMsPacMan_GameModel.class,
                ArcadeMsPacMan_UIConfig.class
            )
            .startPage(ArcadeMsPacMan_StartPage.class, GAME_VARIANT)
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
        if (ui != null) {
            ui.terminate();
        }
    }
}