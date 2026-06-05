/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.ui.app.GamesContainer;
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameFlow;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariant.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = NES_SCREEN_ASPECT_RATIO; // 32:30
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of available height

    private final GamesContainer gamesContainer = new GamesContainer(CoinMechanism.OUT_OF_SERVICE);
    private AppContext context;

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        context = GameUI_Builder
            .newUI(primaryStage, sceneSize.x(), sceneSize.y(), gamesContainer)
            .game(TENGEN_MS_PACMAN,
                () -> new TengenMsPacMan_GameModel(new TengenMsPacMan_GameFlow(gamesContainer)), TengenMsPacMan_UIConfig::new)
            .startPage(TengenMsPacMan_StartPage::new)
            .build();

        context.ui().subViews().gamePlayView().configureDashboard(List.of(
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT
        ), context.ui().translations());

        context.ui().subViews().gamePlayView().dashboard().addSection(
            TengenMsPacMan_DashboardID.JOYPAD,
            new DashboardSectionJoypad(context.ui().subViews().gamePlayView().dashboard()),
            TengenMsPacMan_UIConfig.TEXT_BUNDLE.getString("infobox.joypad.title"),
            false);

        context.displayOnScreen();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.terminate();
        }
    }
}