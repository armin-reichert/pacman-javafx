/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = NES_SCREEN_ASPECT_RATIO; // 32:30
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of available height

    private PacManGamesMachine gamesCollection;
    private Game game;

    @Override
    public void init() {
        gamesCollection = new PacManGamesMachine();
        gamesCollection.insertCartridge(TENGEN_MS_PACMAN.name(), TengenMsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = GameBuilder.compose(gamesCollection, sceneSize.x(), sceneSize.y())
            .gameVariant(TENGEN_MS_PACMAN.name(), false)
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
            game.gameVariantRuntime(TENGEN_MS_PACMAN.name()).uiConfig().translate("infobox.joypad.title"),
            false);

        game.selectGameVariant(TENGEN_MS_PACMAN.name());
        game.show(primaryStage);
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}