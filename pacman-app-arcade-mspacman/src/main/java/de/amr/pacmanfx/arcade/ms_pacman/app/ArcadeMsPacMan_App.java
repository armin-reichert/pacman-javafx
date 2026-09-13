/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.game.GameBox;
import de.amr.pacmanfx.game.GameBuilder;
import de.amr.pacmanfx.game.PacManGamesMasterApp;
import de.amr.pacmanfx.uilib.TimelineGameClock;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    private GameBox gameBox;

    private PacManGamesMasterApp app;

    @Override
    public void init() {
        gameBox = new GameBox(99, new TimelineGameClock());
    }

    @Override
    public void start(Stage stage) {
        app = new GameBuilder()
            .cartridges(ArcadeMsPacMan_Cartridge.CARTRIDGE)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(1.2, 0.8)
            .build(gameBox)
            .orElse(null);

        if (app != null) {
            app.showGameVariant(GameVariantID.ARCADE_MS_PACMAN);
        }
    }

    @Override
    public void stop() {
        if (app != null) {
            app.terminate();
        }
    }
}