/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.XXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.XXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.CartridgeRepository;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.PacManGameCollection;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.TimelineGameClock;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManAllGamesNoBuilderApp extends Application {

    static final float ASPECT_RATIO    = 1.6f; // 16:10
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private GameBox gameBox;
    private PacManGameCollection game;
    private boolean includeTests;

    @Override
    public void init() {
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));

        gameBox = new GameBox(
            new CartridgeRepository(),
            new CoinMechanism(99),
            new TimelineGameClock()
        );

        gameBox.cartridgeRepository().insertCartridges(
            ArcadePacMan_Cartridge.CARTRIDGE,
            ArcadeMsPacMan_Cartridge.CARTRIDGE,
            TengenMsPacMan_Cartridge.CARTRIDGE,
            null,
            XXL_PacMan_Cartridge.CARTRIDGE,
            null,
            XXL_MsPacMan_Cartridge.CARTRIDGE
        );
    }

    @Override
    public void start(Stage stage) {
        game = new PacManGameCollection(gameBox);

        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final GameUI ui = new GameUI(
            stage, sceneSize.x(), sceneSize.y(),
            GameUI.DEFAULT_UI_SETTINGS,
            TengenDashboardFactory.instance()
        );

        final StartPagesView startPages = ui.views().assertView(GameViewID.START_PAGES, StartPagesView.class);
        startPages.addStartPage(game, new ArcadePacMan_StartPage());
        startPages.addStartPage(game, new ArcadeMsPacMan_StartPage());
        startPages.addStartPage(game, new TengenMsPacMan_StartPage());
        startPages.addStartPage(game, new XXL_StartPage());

        game.watchdog().addEventListener(XXL_WorldMapManager.instance());

        game.setUI(ui);
        game.showGameVariant(GameVariantID.ARCADE_PACMAN);

        // This must happen *after* UI has been set!
        startPages.rootPane().setSelectedIndex(0);
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}