/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameExtension;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManAllGamesNoBuilderApp extends Application {

    static final float ASPECT_RATIO    = 1.6f; // 16:10
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    Game game;
    boolean includeTests;

    @Override
    public void init() {
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        PacManGamesMachine.instance().loadCartridges(
            ArcadePacMan_Cartridge.CARTRIDGE,
            ArcadeMsPacMan_Cartridge.CARTRIDGE,
            TengenMsPacMan_Cartridge.CARTRIDGE,
            PacManXXL_PacMan_Cartridge.CARTRIDGE,
            PacManXXL_MsPacMan_Cartridge.CARTRIDGE
        );
        game = new PacManGamesCollection();

        Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        GameUI ui = game.createUI(
            GameUI.DEFAULT_SETTINGS,
            TengenDashboardFactory.instance(),
            stage, sceneSize.x(), sceneSize.y()
        );

        StartPagesView start = ui.viewManager().assertView(GameViewID.START_PAGES, StartPagesView.class);
        start.addStartPage(game, new ArcadePacMan_StartPage());
        start.addStartPage(game, new ArcadeMsPacMan_StartPage());
        start.addStartPage(game, new TengenMsPacMan_StartPage());
        start.addStartPage(game, new PacManXXL_StartPage());
        start.rootPane().setSelectedIndex(0);

        game.extensions().add(new GameExtension(Arcade_GameExtensions.ACTIONS, Arcade_Actions::new));
        game.extensions().add(new GameExtension(TengenMsPacMan_GameExtension.ACTIONS, TengenMsPacMan_Actions::new));
        game.extensions().add(new GameExtension(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings::new));

        game.machine().watchdog().addEventListener(PacManXXL_MapSelector.instance());

        game.setUI(ui);
        game.showUI(GameVariantID.ARCADE_PACMAN);
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}