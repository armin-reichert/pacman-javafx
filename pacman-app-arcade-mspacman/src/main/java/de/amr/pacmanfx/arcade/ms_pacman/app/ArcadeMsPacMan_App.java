/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.views.dashboard.DashboardID;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class ArcadeMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    @Override
    public void init() {
        machine.loadCartridge(ArcadeMsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        final Vector2i screenSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = new GameBuilder(machine, screenSize.x(), screenSize.y())
            .startPage(ArcadeMsPacMan_StartPage::new)
            .build(stage);

        game.ui().views().gamePlayView().dashboard().addCommonSections(game.ui().translations(), List.of(
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.KEYS_GLOBAL,
            DashboardID.KEYS_LOCAL,
            DashboardID.ABOUT)
        );

        game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));

        game.showUI(GameVariantID.ARCADE_MS_PACMAN);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}