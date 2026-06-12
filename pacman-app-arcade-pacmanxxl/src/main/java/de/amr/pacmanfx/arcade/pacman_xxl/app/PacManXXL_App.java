/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardID;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class PacManXXL_App extends Application {

    private static final double ASPECT_RATIO    = 1.6;
    private static final double HEIGHT_FRACTION = 0.8;

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    @Override
    public void init() {
        machine.insertCartridge(PacManXXL_PacMan_Cartridge.CARTRIDGE);
        machine.insertCartridge(PacManXXL_MsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
            .startPage(PacManXXL_StartPage::new)
            .build();

        final GamePlayView playView = game.ui().subViews().gamePlayView();
        playView.configureDashboard(List.of(
            DashboardID.README,
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.CUSTOM_MAPS,
            DashboardID.KEYS_GLOBAL,
            DashboardID.KEYS_LOCAL,
            DashboardID.ABOUT
        ), game.ui().translations());

        final PacManXXL_MapSelector sharedMapSelector = new PacManXXL_MapSelector();
        game.watchdog().addEventListener(sharedMapSelector);

        game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(sharedMapSelector);
        game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(sharedMapSelector);

        game.show(GameVariantID.ARCADE_PACMAN_XXL, primaryStage);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}