/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc. ("all you can f*** ähm play").
 */
public class PacManGames3dApp extends Application {

    static boolean USE_BUILDER = false;

    private static final float ASPECT_RATIO = 1.6f; // 16:10 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private PacManXXL_Common_MapSelector xxlCommonMapSelector;
    private GameUI ui;

    private void addTestStates(Game game) {
        game.control().stateMachine().addState(new LevelShortTestState());
        game.control().stateMachine().addState(new LevelMediumTestState());
        game.control().stateMachine().addState(new CutScenesTestState());

    }
    private GameUI createUI_WithoutBuilder(Stage stage, double sceneWidth, double sceneHeight) {
        {
            String variantName = StandardGameVariant.PACMAN.name();
            Game game = new ArcadePacMan_GameModel(THE_GAME_BOX, THE_GAME_BOX.highScoreFile(variantName));
            addTestStates(game);
            THE_GAME_BOX.registerGame(variantName, game);
        }
        {
            String variantName = StandardGameVariant.MS_PACMAN.name();
            Game game = new ArcadeMsPacMan_GameModel(THE_GAME_BOX, THE_GAME_BOX.highScoreFile(variantName));
            addTestStates(game);
            THE_GAME_BOX.registerGame(variantName, game);
        }
        {
            String variantName = StandardGameVariant.MS_PACMAN_TENGEN.name();
            Game game = new TengenMsPacMan_GameModel(THE_GAME_BOX.highScoreFile(variantName));
            addTestStates(game);
            THE_GAME_BOX.registerGame(variantName, game);
        }
        {
            String variantName = StandardGameVariant.PACMAN_XXL.name();
            Game game = new PacManXXL_PacMan_GameModel(THE_GAME_BOX, xxlCommonMapSelector, THE_GAME_BOX.highScoreFile(variantName));
            addTestStates(game);
            THE_GAME_BOX.registerGame(variantName, game);
        }
        {
            String variantName = StandardGameVariant.MS_PACMAN_XXL.name();
            Game game = new PacManXXL_MsPacMan_GameModel(THE_GAME_BOX, xxlCommonMapSelector, THE_GAME_BOX.highScoreFile(variantName));
            addTestStates(game);
            THE_GAME_BOX.registerGame(variantName, game);
        }

        final Map<String, Class<?>> configClassMap = Map.of(
            StandardGameVariant.PACMAN.name(),           ArcadePacMan_UIConfig.class,
            StandardGameVariant.MS_PACMAN.name(),        ArcadeMsPacMan_UIConfig.class,
            StandardGameVariant.MS_PACMAN_TENGEN.name(), TengenMsPacMan_UIConfig.class,
            StandardGameVariant.PACMAN_XXL.name(),       PacManXXL_PacMan_UIConfig.class,
            StandardGameVariant.MS_PACMAN_XXL.name(),    PacManXXL_MsPacMan_UIConfig.class
        );
        GameUI ui = new GameUI_Implementation(configClassMap, THE_GAME_BOX, stage, sceneWidth, sceneHeight);

        ui.startPagesView().addStartPage(new ArcadePacMan_StartPage(ui));
        ui.startPagesView().addStartPage(new ArcadeMsPacMan_StartPage(ui));
        ui.startPagesView().addStartPage(new TengenMsPacMan_StartPage(ui));
        ui.startPagesView().addStartPage(new PacManXXL_Common_StartPage(ui));

        ui.startPagesView().setSelectedIndex(0);

        ui.dashboard().configure(List.of(
            DashboardID.GENERAL,
            DashboardID.GAME_CONTROL,
            DashboardID.ANIMATION_INFO,
            DashboardID.SETTINGS_3D,
            DashboardID.GAME_INFO,
            DashboardID.ACTOR_INFO,
            DashboardID.CUSTOM_MAPS,
            DashboardID.KEYS_GLOBAL,
            DashboardID.KEYS_LOCAL,
            DashboardID.ABOUT)
        );

        return ui;
    }

    private GameUI createUI_WithBuilder(Stage stage, double sceneWidth, double sceneHeight) {
        return GameUI_Builder.create(stage, sceneWidth, sceneHeight)
            .game(
                StandardGameVariant.PACMAN.name(),
                ArcadePacMan_GameModel.class,
                ArcadePacMan_UIConfig.class)

            .game(
                StandardGameVariant.MS_PACMAN.name(),
                ArcadeMsPacMan_GameModel.class,
                ArcadeMsPacMan_UIConfig.class)

            .game(
                StandardGameVariant.MS_PACMAN_TENGEN.name(),
                TengenMsPacMan_GameModel.class,
                TengenMsPacMan_UIConfig.class)

            .game(
                StandardGameVariant.PACMAN_XXL.name(),
                PacManXXL_PacMan_GameModel.class,
                xxlCommonMapSelector,
                PacManXXL_PacMan_UIConfig.class)

            .game(
                StandardGameVariant.MS_PACMAN_XXL.name(),
                PacManXXL_MsPacMan_GameModel.class,
                xxlCommonMapSelector,
                PacManXXL_MsPacMan_UIConfig.class)

            .startPage(
                ArcadePacMan_StartPage.class,
                StandardGameVariant.PACMAN.name())

            .startPage(
                ArcadeMsPacMan_StartPage.class,
                StandardGameVariant.MS_PACMAN.name())

            .startPage(
                TengenMsPacMan_StartPage.class,
                StandardGameVariant.MS_PACMAN_TENGEN.name())

            .startPage(
                PacManXXL_Common_StartPage.class,
                StandardGameVariant.PACMAN_XXL.name(), StandardGameVariant.MS_PACMAN_XXL.name())

            .dashboard(
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.ANIMATION_INFO,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.CUSTOM_MAPS,
                DashboardID.KEYS_GLOBAL,
                DashboardID.KEYS_LOCAL,
                DashboardID.ABOUT)

            .build();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getBounds().getHeight());
            final int width  = Math.round(ASPECT_RATIO * height);

            xxlCommonMapSelector = new PacManXXL_Common_MapSelector(GameBox.CUSTOM_MAP_DIR);

            ui = USE_BUILDER
                ? createUI_WithBuilder(primaryStage, width, height)
                : createUI_WithoutBuilder(primaryStage, width, height);

            ui.directoryWatchdog().addEventListener(watchEvents -> {
                if (!watchEvents.isEmpty()) {
                    xxlCommonMapSelector.customMapPrototypes().clear();
                    xxlCommonMapSelector.loadCustomMapPrototypes();
                }
            });
            ui.showUI();
        }
        catch (RuntimeException x) {
            Logger.error(x);
            Logger.error("An error occurred on starting the game.");
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (ui != null) {
            ui.terminate();
        }
    }
}