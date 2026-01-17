/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.tengenmspacman.InfoBoxJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import static de.amr.pacmanfx.model.StandardGameVariant.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_ASPECT;

public class TengenMsPacMan_App extends Application {

    private static final String GAME_VARIANT_NAME = TENGEN_MS_PACMAN.name();

    private static final float ASPECT_RATIO = NES_ASPECT; // 32:30 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getVisualBounds().getHeight());
        final int width  = Math.round(ASPECT_RATIO * height);
        ui = GameUI_Builder.create(primaryStage, width, height)
            .game(
                GAME_VARIANT_NAME,
                TengenMsPacMan_GameModel.class,
                TengenMsPacMan_UIConfig.class
            )
            .startPage(TengenMsPacMan_StartPage.class, GAME_VARIANT_NAME)
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

        ui.viewManager().playView().dashboard().addInfoBox(
            TengenMsPacMan_DashboardID.JOYPAD,
            TengenMsPacMan_UIConfig.TEXT_BUNDLE.getString("infobox.joypad.title"),
            new InfoBoxJoypad(ui));

        ui.show();
    }

    @Override
    public void stop() {
        if (ui != null) {
            ui.terminate();
        }
    }
}