/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.app;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;

import java.io.File;

public class ArcadeMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private static final File HIGH_SCORE_FILE = GameContext.highScoreFile(GameVariant.ARCADE_MS_PACMAN);

    private final GameBox gameBox = new GameBox();
    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final Dimension2D screenSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        ui = GameUI_Builder
            .newUI(primaryStage, screenSize.getWidth(), screenSize.getHeight(), gameBox)
            .game(GameVariant.ARCADE_MS_PACMAN,
                () -> new ArcadeMsPacMan_GameModel(gameBox.coinMechanism(), HIGH_SCORE_FILE),
                ArcadeMsPacMan_UIConfig::new)
            .startPage(ArcadeMsPacMan_StartPage::new)
            .dashboard(
                CommonDashboardID.GENERAL,
                CommonDashboardID.GAME_CONTROL,
                CommonDashboardID.SETTINGS_3D,
                CommonDashboardID.GAME_INFO,
                CommonDashboardID.ACTOR_INFO,
                CommonDashboardID.KEYS_GLOBAL,
                CommonDashboardID.KEYS_LOCAL,
                CommonDashboardID.ABOUT)
            .build();
        ui.show();
    }

    @Override
    public void stop() {
        if (ui != null) {
            ui.terminate();
        }
    }
}