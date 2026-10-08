/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.PacManGameEngineBuilder;
import de.amr.pacmanfx.game.PacManGamesEngineImpl;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private GameBox gameBox;
    private PacManGamesEngineImpl engine;

    @Override
    public void init() {
        gameBox = new GameBox();
    }

    @Override
    public void start(Stage stage) {
        engine = new PacManGameEngineBuilder()
            .cartridges(
                XXL_PacMan_Cartridge.CARTRIDGE,
                XXL_MsPacMan_Cartridge.CARTRIDGE)
            .startPage(XXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .buildEngine(gameBox)
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