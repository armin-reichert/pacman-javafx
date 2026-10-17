/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.app.config.PacManGameEngineBuilder;
import de.amr.pacmanfx.app.runtime.GameEngine;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.PlayStation;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private GameEngine engine;

    @Override
    public void init() {
        PlayStation.instance().insertCartridges(
            XXL_PacMan_Cartridge.CARTRIDGE,
            XXL_MsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .startPage(() -> new XXL_StartPage("Pac-Man XXL games")) //TODO localize
            .window(stage)
            .screenArea(1.6, 0.8)
            .buildEngine()
            .orElseThrow(IllegalStateException::new);

        engine.watchdog().addEventListener(XXL_WorldMapManager.instance());
        engine.showGameVariant(GameVariantID.ARCADE_PACMAN_XXL);
    }

    @Override
    public void stop() {
        engine.terminate();
    }
}