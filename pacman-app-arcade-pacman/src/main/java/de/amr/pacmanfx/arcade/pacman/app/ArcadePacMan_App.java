/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.CartridgeRepository;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.GameBuilder;
import de.amr.pacmanfx.game.PacManGamesMasterApp;
import de.amr.pacmanfx.uilib.TimelineGameClock;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    private GameBox gameBox;

    private PacManGamesMasterApp game;

    @Override
    public void init() throws Exception {
        gameBox = new GameBox(
            new CartridgeRepository(),
            new CoinMechanism(99),
            new TimelineGameClock()
        );
    }

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(ArcadePacMan_Cartridge.CARTRIDGE)
            .uiSettings(getClass().getResource("/de/amr/pacmanfx/arcade/pacman/ui.json"))
            .startPage(ArcadePacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .build(gameBox)
            .orElse(null);

        if (game != null) {
            game.showGameVariant(GameVariantID.ARCADE_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (game != null) game.terminate();
    }
}