/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

public class ArcadePacMan_App extends Application {

    private static final String GAME_VARIANT_NAME = StandardGameVariant.PACMAN.name();

    private static final float ASPECT_RATIO = 1.2f; // 12:10 aspect ratio
    private static final float USED_HEIGHT_FRACTION = 0.8f;  // 80% of available height

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
        final var game = new ArcadePacMan_GameModel(THE_GAME_BOX, THE_GAME_BOX.highScoreFile(GAME_VARIANT_NAME));
        THE_GAME_BOX.registerGame(GAME_VARIANT_NAME, game);

        final Map<String, Class<? extends GameUI_Config>> uiConfigMap = Map.of(GAME_VARIANT_NAME, ArcadePacMan_UIConfig.class);
        final var ui = new GameUI_Implementation(uiConfigMap, THE_GAME_BOX, stage, sceneWidth, sceneHeight);

        final var startPage = new ArcadePacMan_StartPage();
        ui.startPagesView().addStartPage(startPage);
        ui.startPagesView().setSelectedIndex(0);

        ui.dashboard().configure(List.of(
            CommonDashboardID.GENERAL,
            CommonDashboardID.GAME_CONTROL,
            CommonDashboardID.SETTINGS_3D,
            CommonDashboardID.GAME_INFO,
            CommonDashboardID.ACTOR_INFO,
            CommonDashboardID.KEYS_GLOBAL,
            CommonDashboardID.KEYS_LOCAL,
            CommonDashboardID.ABOUT)
        );

        startPage.init(ui);
        return ui;
    }

    private GameUI createUI_WithBuilder(Stage stage, double sceneWidth, double sceneHeight) {
        return GameUI_Builder.create(stage, sceneWidth, sceneHeight)
            .game(
                GAME_VARIANT_NAME,
                ArcadePacMan_GameModel.class,
                ArcadePacMan_UIConfig.class
            )
            .startPage(ArcadePacMan_StartPage.class, GAME_VARIANT_NAME)
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
    }
}