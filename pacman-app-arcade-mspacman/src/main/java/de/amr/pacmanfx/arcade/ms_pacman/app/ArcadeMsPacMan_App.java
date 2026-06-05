/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.ui.app.GamesContainer;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.uilib.GameClockFX;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class ArcadeMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private AppContext context;

    @Override
    public void start(Stage primaryStage) {
        final var gameBox = new GamesContainer(new GameClockFX(), new CoinMechanism(99));
        final Vector2i screenSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        context = GameUI_Builder
            .newUI(primaryStage, screenSize.x(), screenSize.y(), gameBox)
            .game(GameVariant.ARCADE_MS_PACMAN,
                () -> new ArcadeMsPacMan_GameModel(new Arcade_GameFlow(gameBox), gameBox.coinMechanism()),
                ArcadeMsPacMan_UIConfig::new)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .build();

        context.ui().subViews().gamePlayView().dashboard().addCommonSections(context.ui().translations(), List.of(
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT)
        );
        context.displayOnScreen();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.terminate();
        }
    }
}