/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_Cartridge;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
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

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    @Override
    public void init() {
        machine.insertCartridge(GameVariantID.ARCADE_PACMAN_XXL.name(),    PacManXXL_PacMan_Cartridge.CARTRIDGE);
        machine.insertCartridge(GameVariantID.ARCADE_MS_PACMAN_XXL.name(), PacManXXL_MsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
            .gameVariant(GameVariantID.ARCADE_PACMAN_XXL)
            .gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL)
            .startPage(PacManXXL_StartPage::new)
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
                sectionCustomMaps.setMapEditFunction(mapFile -> CommonActions.createEditMapFileAction(mapFile).execute(game));
            }
        });

        final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();
        game.watchdog().addEventListener(xxlMapSelector);

        game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(xxlMapSelector);
        game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(xxlMapSelector);

        game.show(GameVariantID.ARCADE_PACMAN_XXL, primaryStage);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}