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
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManAllGamesNoBuilder extends Application {

    static final float ASPECT_RATIO    = 1.6f; // 16:10
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private PacManGamesEngine engine;

    private boolean includeTests;

    @Override
    public void init() {
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
        PlayStation.instance().insertCartridges(
            ArcadePacMan_Cartridge.CARTRIDGE,
            ArcadeMsPacMan_Cartridge.CARTRIDGE,
            TengenMsPacMan_Cartridge.CARTRIDGE,
            null,
            XXL_PacMan_Cartridge.CARTRIDGE,
            null,
            XXL_MsPacMan_Cartridge.CARTRIDGE
        );
        engine = new PacManGamesEngine();
    }

    @Override
    public void start(Stage stage) {

        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final GameUI ui = new GameUI(stage, sceneSize.x(), sceneSize.y(), GameUI.DEFAULT_UI_SETTINGS);

        final StartPagesView startPages = ui.viewManager().reqView(GameViewID.START_PAGES, StartPagesView.class);
        startPages.addStartPage(engine, new ArcadePacMan_StartPage());
        startPages.addStartPage(engine, new ArcadeMsPacMan_StartPage());
        startPages.addStartPage(engine, new TengenMsPacMan_StartPage());
        startPages.addStartPage(engine, new XXL_StartPage());

        engine.watchdog().addEventListener(XXL_WorldMapManager.instance());

        engine.setUI(ui, TengenDashboardFactory.instance());
        engine.showGameVariant(GameVariantID.ARCADE_PACMAN);

        // This must happen *after* UI has been set!
        startPages.rootPane().setSelectedIndex(0);
    }

    @Override
    public void stop() {
        if (engine != null) {
            engine.terminate();
        }
    }
}