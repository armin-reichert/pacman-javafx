/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;

import static de.amr.pacmanfx.model.GameVariant.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_ASPECT;

public class TengenMsPacMan_App extends Application {

    private static final String NAME_OF_THE_GAME = TENGEN_MS_PACMAN.name();

    private static final float ASPECT_RATIO    = NES_ASPECT; // 32:30
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of available height

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final Dimension2D sceneSize = Ufx.computeSceneSize(ASPECT_RATIO, HEIGHT_FRACTION);

        ui = GameUI_Builder
            .create(primaryStage, sceneSize.getWidth(), sceneSize.getHeight())
            .game(NAME_OF_THE_GAME, TengenMsPacMan_GameModel.class, TengenMsPacMan_UIConfig::new)
            .startPage(TengenMsPacMan_StartPage.class, NAME_OF_THE_GAME)
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

        final Dashboard dashboard = ui.views().playView().dashboard();
        dashboard.addSection(
            TengenMsPacMan_DashboardID.JOYPAD,
            new DashboardSectionJoypad(dashboard),
            TengenMsPacMan_UIConfig.TEXT_BUNDLE.getString("infobox.joypad.title"),
            false);

        ui.show();
    }

    @Override
    public void stop() {
        if (ui != null) {
            ui.terminate();
        }
    }
}