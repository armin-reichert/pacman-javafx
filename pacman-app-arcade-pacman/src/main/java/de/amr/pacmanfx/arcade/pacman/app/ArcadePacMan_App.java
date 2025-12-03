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
import de.amr.pacmanfx.ui.api.StartPage;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

public class ArcadePacMan_App extends Application {

    static boolean USE_BUILDER = false;

    private static final String GAME_VARIANT_NAME = StandardGameVariant.PACMAN.name();

    private static final float ASPECT_RATIO = 1.2f; // 12:10 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private GameUI ui;

    private GameUI createUI_WithoutBuilder(Stage stage, double sceneWidth, double sceneHeight) {
        var game = new ArcadePacMan_GameModel(THE_GAME_BOX, THE_GAME_BOX.highScoreFile(GAME_VARIANT_NAME));
        THE_GAME_BOX.registerGame(GAME_VARIANT_NAME, game);

        Map<String, Class<?>> configClassMap = Map.of(
            GAME_VARIANT_NAME, ArcadePacMan_UIConfig.class
        );
        GameUI ui = new GameUI_Implementation(configClassMap, THE_GAME_BOX, stage, sceneWidth, sceneHeight);

        StartPage startPage = new ArcadePacMan_StartPage(ui);
        ui.startPagesView().addStartPage(startPage);
        ui.startPagesView().setSelectedIndex(0);

        ui.playView().dashboard().configure(List.of(
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.KEYBOARD_SHORTCUTS_GLOBAL,
            DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
            DashboardID.ABOUT)
        );

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
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.KEYBOARD_SHORTCUTS_GLOBAL,
                DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                DashboardID.ABOUT)
            .build();
    }

    @Override
    public void start(Stage primaryStage) {
        final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getBounds().getHeight());
        final int width  = Math.round(ASPECT_RATIO * height);
        ui = USE_BUILDER
            ? createUI_WithBuilder(primaryStage, width, height)
            : createUI_WithoutBuilder(primaryStage, width, height);
        ui.showUI();
    }

    @Override
    public void stop() {
        ui.terminate();
    }
}