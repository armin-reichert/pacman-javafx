/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.app.config.PacManGameEngineBuilder;
import de.amr.pacmanfx.app.runtime.GameEngine;
import de.amr.pacmanfx.engine.CartridgeRegistry;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private GameEngine engine;

    @Override
    public void init() {
        CartridgeRegistry.instance().register(TengenMsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .dashboardFactory(TengenDashboardFactory.instance())
            .startPage(TengenMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(NES_SCREEN_ASPECT_RATIO, 0.8)
            .buildEngine()
            .orElseThrow(IllegalStateException::new);

        engine.show(TENGEN_MS_PACMAN);
    }

    @Override
    public void stop() {
        engine.terminate();
    }
}