/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.Cartridge;
import de.amr.pacmanfx.ui.game.GamesCollection;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class PacManXXL_App extends Application {

    private static final double ASPECT_RATIO    = 1.6;
    private static final double HEIGHT_FRACTION = 0.8;

    private GamesCollection gamesCollection;
    private Game game;

    @Override
    public void init() {
        gamesCollection = new GamesCollection();
        gamesCollection.registerGame(GameVariant.ARCADE_PACMAN_XXL.name(), new Cartridge(
            Arcade_GameFlow::new,
            PacManXXL_PacMan_GameModel::new,
            PacManXXL_PacMan_GameRules::new,
            PacManXXL_PacMan_UIConfig::new
        ));
        gamesCollection.registerGame(GameVariant.ARCADE_MS_PACMAN_XXL.name(), new Cartridge(
            Arcade_GameFlow::new,
            PacManXXL_MsPacMan_GameModel::new,
            PacManXXL_MsPacMan_GameRules::new,
            PacManXXL_MsPacMan_UIConfig::new
        ));
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = GameBuilder.compose(gamesCollection, primaryStage, sceneSize.x(), sceneSize.y())
            .gameVariant(GameVariant.ARCADE_PACMAN_XXL.name(), false)
            .gameVariant(GameVariant.ARCADE_MS_PACMAN_XXL.name(), false)
            .startPage(PacManXXL_StartPage::new)
            .coinMechanism(true)
            .build();

        final GamePlayView playView = game.ui().subViews().gamePlayView();
        playView.configureDashboard(List.of(
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
        ), game.ui().translations());

        playView.dashboard().findSection(CommonDashboardID.CUSTOM_MAPS).ifPresent(section -> {
            if (section instanceof DashboardSectionCustomMaps sectionCustomMaps) {
                sectionCustomMaps.setCustomDirWatchDog(game.watchdog());
                sectionCustomMaps.setMapEditFunction(mapFile -> CommonActions.editMapFile(game, mapFile));
            }
        });

        final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();
        game.watchdog().addEventListener(xxlMapSelector);

        game.gameVariantRuntime(GameVariant.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(xxlMapSelector);
        game.gameVariantRuntime(GameVariant.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(xxlMapSelector);

        game.displayOnScreen();
    }

    @Override
    public void stop() {
        game.terminate();
    }
}