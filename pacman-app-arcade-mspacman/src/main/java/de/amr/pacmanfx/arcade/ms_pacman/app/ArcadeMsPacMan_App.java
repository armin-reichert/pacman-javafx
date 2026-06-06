/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameAppBuilder;
import de.amr.pacmanfx.ui.app.GamesContainer;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class ArcadeMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private final CoinMechanism coinMechanism = new CoinMechanism(99);
    private AppContext app;

    @Override
    public void start(Stage primaryStage) {
        final var gamesContainer = new GamesContainer();
        final Vector2i screenSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        app = GameAppBuilder
            .newApp(primaryStage, screenSize.x(), screenSize.y(), gamesContainer, coinMechanism)
            .game(
                GameVariant.ARCADE_MS_PACMAN,
                Arcade_GameFlow::new,
                () -> new ArcadeMsPacMan_GameModel(coinMechanism),
                ArcadeMsPacMan_GameRules::new,
                ArcadeMsPacMan_UIConfig::new
            )
            .startPage(ArcadeMsPacMan_StartPage::new)
            .build();

        app.ui().subViews().gamePlayView().dashboard().addCommonSections(app.ui().translations(), List.of(
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT)
        );
        app.displayOnScreen();
    }

    @Override
    public void stop() {
        if (app != null) {
            app.terminate();
        }
    }
}