/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameFlow;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GamesCollection;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariant.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = NES_SCREEN_ASPECT_RATIO; // 32:30
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of available height

    private GamesCollection gamesCollection;
    private Game game;

    @Override
    public void init() {
        gamesCollection = new GamesCollection();
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = GameBuilder.compose(gamesCollection, primaryStage, sceneSize.x(), sceneSize.y())

            .gameVariant(
                TENGEN_MS_PACMAN,
                TengenMsPacMan_GameFlow::new,
                TengenMsPacMan_GameModel::new,
                TengenMsPacMan_GameRules::new,
                TengenMsPacMan_UIConfig::new
            )
            .startPage(TengenMsPacMan_StartPage::new)
            .build();

        final GamePlayView playView = game.ui().subViews().gamePlayView();

        playView.configureDashboard(List.of(
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT
        ), game.ui().translations());

        // Will be added before "ABOUT" section!
        playView.dashboard().addSection(
            TengenMsPacMan_DashboardID.JOYPAD,
            new DashboardSectionJoypad(playView.dashboard()),
            game.ui().configurations().getOrCreateUIConfig(TENGEN_MS_PACMAN.name()).translate("infobox.joypad.title"),
            false);

        game.displayOnScreen();
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}