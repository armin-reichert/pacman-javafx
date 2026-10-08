/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.PacManGameEngineBuilder;
import de.amr.pacmanfx.game.PacManGameEngine;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    private GameBox gameBox;

    private PacManGameEngine app;

    @Override
    public void init() {
        gameBox = new GameBox();
    }

    @Override
    public void start(Stage stage) {
        app = new PacManGameEngineBuilder()
            .cartridges(ArcadePacMan_Cartridge.CARTRIDGE)
            .uiSettings(getClass().getResource("/de/amr/pacmanfx/arcade/pacman/ui.json"))
            .startPage(ArcadePacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .buildEngine(gameBox)
            .orElse(null);

        if (app != null) {
            app.showGameVariant(GameVariantID.ARCADE_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (app != null) app.terminate();
    }
}