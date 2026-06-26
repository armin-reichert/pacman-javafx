/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadePacMan_App extends Application {

    static final float ASPECT_RATIO    = 1.2f; // 12:10 aspect ratio
    static final float HEIGHT_FRACTION = 0.8f; // 80% of available height

    Game game;

    @Override
    public void start(Stage stage) {
        new GameBuilder()
            .cartridges(ArcadePacMan_Cartridge.CARTRIDGE)
            .uiSettings(getClass().getResource("/de/amr/pacmanfx/arcade/pacman/ui.json"))
            .startPage(ArcadePacMan_StartPage::new)
            .window(stage)
            .screenArea(ASPECT_RATIO, HEIGHT_FRACTION)
            .build()
            .ifPresent(game -> {
                game.showUI(GameVariantID.ARCADE_PACMAN);
                this.game = game;
            });
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}