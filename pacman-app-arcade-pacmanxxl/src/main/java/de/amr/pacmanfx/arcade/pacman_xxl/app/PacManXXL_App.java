/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class PacManXXL_App extends Application {

    private static final double ASPECT_RATIO    = 1.6;
    private static final double HEIGHT_FRACTION = 0.8;

    private final GameBox gameBox = new GameBox(new CoinMechanism(99));
    private AppContext context;

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final var mapSelector = new PacManXXL_MapSelector(gameBox.customMapDir());

        context = GameUI_Builder
            .newUI(primaryStage, sceneSize.x(), sceneSize.y(), gameBox)
            .game(GameVariant.ARCADE_PACMAN_XXL,
                () -> new PacManXXL_PacMan_GameModel(gameBox.coinMechanism(), mapSelector),
                PacManXXL_PacMan_UIConfig::new)
            .game(GameVariant.ARCADE_MS_PACMAN_XXL,
                () -> new PacManXXL_MsPacMan_GameModel(gameBox.coinMechanism(), mapSelector),
                PacManXXL_MsPacMan_UIConfig::new)
            .startPage(PacManXXL_StartPage::new)
            .build();

        context.ui().subViews().gamePlayView().configureDashboard(List.of(
            CommonDashboardID.README,
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.CUSTOM_MAPS,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT
        ), context.ui().translations());

        context.ui().subViews().gamePlayView().dashboard().findSection(CommonDashboardID.CUSTOM_MAPS)
            .filter(DashboardSectionCustomMaps.class::isInstance)
            .map(DashboardSectionCustomMaps.class::cast)
            .ifPresent(section -> {
                section.setCustomDirWatchDog(context.customDirWatchdog());
                section.setMapEditFunction(mapFile -> context.editMap(mapFile));
            });

        context.customDirWatchdog().addEventListener(mapSelector);
        context.displayOnScreen();
    }

    @Override
    public void stop() {
        context.terminate();
    }
}