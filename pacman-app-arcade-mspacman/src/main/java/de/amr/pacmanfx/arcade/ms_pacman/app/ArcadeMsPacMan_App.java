/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import javafx.application.Application;
import javafx.stage.Stage;

public class ArcadeMsPacMan_App extends Application {

    static final float ASPECT_RATIO    = 1.2f; // 12:10
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    Game game;

    @Override
    public void start(Stage stage) {
        new GameBuilder()
            .cartridges(ArcadeMsPacMan_Cartridge.CARTRIDGE)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .window(stage)
            .screenArea(ASPECT_RATIO, HEIGHT_FRACTION)
            .build()
            .ifPresent(game -> {
                game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));
                game.showUI(GameVariantID.ARCADE_MS_PACMAN);
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