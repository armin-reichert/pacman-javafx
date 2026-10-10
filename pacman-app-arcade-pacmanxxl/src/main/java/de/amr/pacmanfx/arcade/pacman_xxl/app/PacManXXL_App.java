/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.config.PacManGameEngineBuilder;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private PacManGamesEngine engine;

    @Override
    public void init() {
        PlayStation.instance().insertCartridges(
            XXL_PacMan_Cartridge.CARTRIDGE,
            XXL_MsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .startPage(XXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .buildEngine()
            .orElse(null);

        if (engine != null) {
            engine.watchdog().addEventListener(XXL_WorldMapManager.instance());
            engine.showGameVariant(GameVariantID.ARCADE_PACMAN_XXL);
        }
    }

    @Override
    public void stop() {
        if (engine != null) {
            engine.terminate();
        }
    }
}