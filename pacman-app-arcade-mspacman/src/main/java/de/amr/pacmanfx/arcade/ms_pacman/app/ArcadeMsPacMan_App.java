/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.PacManGameEngineBuilder;
import de.amr.pacmanfx.engine.PacManGamesEngineImpl;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    private PacManGamesEngineImpl engine;

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .cartridges(ArcadeMsPacMan_Cartridge.CARTRIDGE)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .buildEngine()
            .orElse(null);

        if (engine != null) {
            engine.showGameVariant(GameVariantID.ARCADE_MS_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (engine != null) {
            engine.terminate();
        }
    }
}