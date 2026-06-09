/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class ArcadeMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private PacManGamesMachine pacManGamesMachine;
    private Game game;

    @Override
    public void init() throws Exception {
        pacManGamesMachine = new PacManGamesMachine();
        pacManGamesMachine.insertCartridge(GameVariantID.ARCADE_MS_PACMAN.name(), ArcadeMsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i screenSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = GameBuilder.compose(pacManGamesMachine, screenSize.x(), screenSize.y())
            .gameVariant(GameVariantID.ARCADE_MS_PACMAN.name())
            .startPage(ArcadeMsPacMan_StartPage::new)
            .build();

        game.ui().subViews().gamePlayView().dashboard().addCommonSections(game.ui().translations(), List.of(
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT)
        );
        game.selectGameVariant(GameVariantID.ARCADE_MS_PACMAN.name());
        game.show(primaryStage);
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }
}