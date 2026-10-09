/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.engine.config.PacManGameEngineBuilder;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngineImpl;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private PacManGamesEngineImpl engine;

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .cartridges(TengenMsPacMan_Cartridge.CARTRIDGE)
            .dashboardFactory(TengenDashboardFactory.instance())
            .startPage(TengenMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(NES_SCREEN_ASPECT_RATIO, 0.8)
            .buildEngine()
            .orElse(null);

        if (engine != null) {
            engine.showGameVariant(TENGEN_MS_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (engine != null) {
            engine.terminate();
        }
    }
}