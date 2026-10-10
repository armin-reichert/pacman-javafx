/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.PlayStation;
import de.amr.pacmanfx.engine.config.PacManGameEngineBuilder;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    private PacManGamesEngine engine;

    @Override
    public void init() {
        PlayStation.instance().insertCartridges(ArcadePacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .uiSettings(getClass().getResource("/de/amr/pacmanfx/arcade/pacman/ui.json"))
            .startPage(ArcadePacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .buildEngine()
            .orElseThrow(IllegalStateException::new);

        engine.showGameVariant(GameVariantID.ARCADE_PACMAN);
    }

    @Override
    public void stop() {
        engine.terminate();
    }
}