/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.game.CartridgeRepository;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.GameBuilder;
import de.amr.pacmanfx.game.PacManGamesMasterApp;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import de.amr.pacmanfx.uilib.TimelineGameClock;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private GameBox gameBox;
    private PacManGamesMasterApp game;

    @Override
    public void init() {
        gameBox = new GameBox(
            new CartridgeRepository(),
            new CoinMechanism(0), // Not used
            new TimelineGameClock()
        );
    }

    @Override
    public void start(Stage stage) {
        game = new GameBuilder()
            .cartridges(TengenMsPacMan_Cartridge.CARTRIDGE)
            .dashboardFactory(TengenDashboardFactory.instance())
            .startPage(TengenMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(NES_SCREEN_ASPECT_RATIO, 0.8)
            .build(gameBox)
            .orElse(null);

        if (game != null) {
            game.showGameVariant(TENGEN_MS_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}