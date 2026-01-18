/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static de.amr.pacmanfx.model.StandardGameVariant.ARCADE_PACMAN;

public class ArcadePacMan_App extends Application {

    private static final String PACMAN_GAME = ARCADE_PACMAN.name();

    private static final float ASPECT_RATIO = 1.2f; // 12:10 aspect ratio
    private static final float USED_HEIGHT_FRACTION = 0.8f;  // 80% of available height

    private static final DashboardID[] DASHBOARD_IDS = {
        CommonDashboardID.GENERAL,
        CommonDashboardID.GAME_CONTROL,
        CommonDashboardID.SETTINGS_3D,
        CommonDashboardID.ANIMATION_INFO,
        CommonDashboardID.GAME_INFO,
        CommonDashboardID.ACTOR_INFO,
        CommonDashboardID.KEYS_GLOBAL,
        CommonDashboardID.KEYS_LOCAL,
        CommonDashboardID.ABOUT
    };

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        final int height = (int) Math.round(USED_HEIGHT_FRACTION * screenHeight);
        final int width  = Math.round(ASPECT_RATIO * height);
        final String useBuilder = getParameters().getNamed().getOrDefault("use_builder", "true");
        try {
            ui = Boolean.parseBoolean(useBuilder)
                ? createUI_WithBuilder(primaryStage, width, height)
                : createUI_WithoutBuilder(primaryStage, width, height);
            ui.show();
        }
        catch (RuntimeException x) {
            Logger.error(x);
            Logger.error("An error occurred starting the game.");
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        ui.terminate();
    }

    private GameUI createUI_WithoutBuilder(Stage stage, double sceneWidth, double sceneHeight) {
        final File highScoreFile = THE_GAME_BOX.highScoreFile(PACMAN_GAME);
        THE_GAME_BOX.registerGame(PACMAN_GAME, new ArcadePacMan_GameModel(THE_GAME_BOX, highScoreFile));
        final Map<String, Class<? extends GameUI_Config>> uiConfigMap = Map.of(PACMAN_GAME, ArcadePacMan_UIConfig.class);
        final var ui = new GameUI_Implementation(uiConfigMap, THE_GAME_BOX, stage, sceneWidth, sceneHeight);
        ui.views().startPagesView().addStartPage(new ArcadePacMan_StartPage());
        ui.views().playView().dashboard().configure(ui, List.of(DASHBOARD_IDS));
        return ui;
    }

    private GameUI createUI_WithBuilder(Stage stage, double sceneWidth, double sceneHeight) {
        return GameUI_Builder.create(stage, sceneWidth, sceneHeight)
            .game(
                PACMAN_GAME,
                ArcadePacMan_GameModel.class,
                ArcadePacMan_UIConfig.class
            )
            .startPage(ArcadePacMan_StartPage.class, PACMAN_GAME)
            .dashboard(DASHBOARD_IDS)
            .build();
    }
}