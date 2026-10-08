/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.PacManGameEngineBuilder;
import de.amr.pacmanfx.game.PacManGamesEngineImpl;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    private PacManGamesEngineImpl engine;

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .cartridges(ArcadePacMan_Cartridge.CARTRIDGE)
            .uiSettings(getClass().getResource("/de/amr/pacmanfx/arcade/pacman/ui.json"))
            .startPage(ArcadePacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .buildEngine()
            .orElse(null);

        if (engine != null) {
            engine.showGameVariant(GameVariantID.ARCADE_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (engine != null) engine.terminate();
    }
}