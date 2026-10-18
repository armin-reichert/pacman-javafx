/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.app.config.PacManGameEngineBuilder;
import de.amr.pacmanfx.app.runtime.GameEngine;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.CartridgeRegistry;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    private GameEngine engine;

    @Override
    public void init() {
        CartridgeRegistry.instance().register(ArcadeMsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .startPage(ArcadeMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .buildEngine()
            .orElseThrow(IllegalStateException::new);

        engine.showGameVariant(GameVariantID.ARCADE_MS_PACMAN);
    }

    @Override
    public void stop() {
        engine.terminate();
    }
}