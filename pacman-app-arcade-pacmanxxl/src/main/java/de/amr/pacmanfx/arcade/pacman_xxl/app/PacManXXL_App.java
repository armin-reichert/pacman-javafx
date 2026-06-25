/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

public class PacManXXL_App extends Application {

    private static final double ASPECT_RATIO    = 1.6;
    private static final double HEIGHT_FRACTION = 0.8;

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    @Override
    public void init() {
        machine.loadCartridge(PacManXXL_PacMan_Cartridge.CARTRIDGE);
        machine.loadCartridge(PacManXXL_MsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
            .startPage(PacManXXL_StartPage::new)
            .build(stage);

        final PacManXXL_MapSelector sharedMapSelector = new PacManXXL_MapSelector();
        game.watchdog().addEventListener(sharedMapSelector);

        game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(sharedMapSelector);
        game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(sharedMapSelector);

        game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));

        game.showUI(GameVariantID.ARCADE_PACMAN_XXL);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}