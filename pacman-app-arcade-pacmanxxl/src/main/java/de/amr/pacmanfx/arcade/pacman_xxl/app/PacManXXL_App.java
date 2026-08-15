/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.CartridgeRepository;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.GameBuilder;
import de.amr.pacmanfx.game.PacManGameCollection;
import de.amr.pacmanfx.uilib.TimelineGameClock;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private GameBox gameBox;
    private PacManGameCollection game;

    @Override
    public void init() {
        gameBox = new GameBox(
            new CartridgeRepository(),
            new CoinMechanism(99),
            new TimelineGameClock()
        );
    }

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(
                XXL_PacMan_Cartridge.CARTRIDGE,
                XXL_MsPacMan_Cartridge.CARTRIDGE)
            .startPage(XXL_StartPage::new)
            .window(stage)
            .screenArea(1.6, 0.8)
            .build(gameBox)
            .orElse(null);

        if (game != null) {
            game.watchdog().addEventListener(XXL_WorldMapManager.instance());
            game.showGameVariant(GameVariantID.ARCADE_PACMAN_XXL);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}