/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.GameImpl;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.application.Application;
import javafx.stage.Stage;

import static de.amr.pacmanfx.uilib.Ufx.computeScreenSectionSize;

public class ArcadePacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10 aspect ratio
    private static final float HEIGHT_FRACTION = 0.8f; // 80% of available height

    private PacManGamesMachine machine;
    private Game game;
    private boolean useBuilder;

    @Override
    public void init() {
        machine = new PacManGamesMachine();
        machine.loadCartridge(ArcadePacMan_Cartridge.CARTRIDGE);
        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        if (useBuilder) {
            game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
                .startPage(ArcadePacMan_StartPage::new)
                .build(GameUI.DEFAULT_SETTINGS, stage);
        }
        else {
            game = new GameImpl(machine);
            game.createUI(GameUI.DEFAULT_SETTINGS, stage, sceneSize.x(), sceneSize.y());
            game.ui().views().assertView(GameViewID.START_PAGES, StartPagesView.class)
                .addStartPage(game, new ArcadePacMan_StartPage());
        }

        game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));
        game.showUI(GameVariantID.ARCADE_PACMAN);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}