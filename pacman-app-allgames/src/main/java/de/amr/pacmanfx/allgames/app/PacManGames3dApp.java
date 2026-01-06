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
import de.amr.pacmanfx.tengenmspacman.InfoBoxJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
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
import static de.amr.pacmanfx.model.StandardGameVariant.*;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc.
 * ("All you can f** ähm play").
 * <p>
 * Command-line arguments:
 * <ul>
 *     <li>{@code --use_builder=value}:<br/>
 *     Any value where "true" != toLower(value) is evaluated to false!</li>
 * </ul>
 * </p>
 */
public class PacManGames3dApp extends Application {

    private static final float ASPECT_RATIO = 1.6f; // 16:10 aspect ratio
    private static final float USED_HEIGHT_FRACTION = 0.8f;  // 80% of screen height

    private static final Map<String, Class<? extends GameUI_Config>> UI_CONFIG_MAP = Map.of(
        PACMAN.name(),           ArcadePacMan_UIConfig.class,
        MS_PACMAN.name(),        ArcadeMsPacMan_UIConfig.class,
        MS_PACMAN_TENGEN.name(), TengenMsPacMan_UIConfig.class,
        PACMAN_XXL.name(),       PacManXXL_PacMan_UIConfig.class,
        MS_PACMAN_XXL.name(),    PacManXXL_MsPacMan_UIConfig.class
    );

    private static final DashboardID[] DASHBOARD_IDS = {
        CommonDashboardID.GENERAL,
        CommonDashboardID.GAME_CONTROL,
        CommonDashboardID.SETTINGS_3D,
        CommonDashboardID.ANIMATION_INFO,
        CommonDashboardID.GAME_INFO,
        CommonDashboardID.ACTOR_INFO,
        CommonDashboardID.CUSTOM_MAPS,
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
        final var xxlMapSelector = new PacManXXL_MapSelector(GameBox.CUSTOM_MAP_DIR);
        final String useBuilder = getParameters().getNamed().getOrDefault("use_builder", "true");
        try {
            ui = Boolean.parseBoolean(useBuilder)
                ? createUI_WithBuilder(primaryStage, width, height, xxlMapSelector)
                : createUI_WithoutBuilder(primaryStage, width, height, xxlMapSelector);

            ui.dashboard().addInfoBox(
                TengenMsPacMan_DashboardID.JOYPAD,
                TengenMsPacMan_UIConfig.TEXT_BUNDLE.getString("infobox.joypad.title"),
                new InfoBoxJoypad(ui));

            ui.customDirWatchdog().addEventListener(xxlMapSelector);
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
        if (ui != null) {
            ui.terminate();
        }
    }

    private GameUI createUI_WithoutBuilder(
        Stage stage,
        double sceneWidth,
        double sceneHeight,
        PacManXXL_MapSelector xxlMapSelector)
    {
        Logger.info("Creating UI without builder");

        registerGameWithTests(PACMAN,           new ArcadePacMan_GameModel(THE_GAME_BOX, highScoreFile(PACMAN)));
        registerGameWithTests(MS_PACMAN,        new ArcadeMsPacMan_GameModel(THE_GAME_BOX, highScoreFile(MS_PACMAN)));
        registerGameWithTests(MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel(highScoreFile(MS_PACMAN_TENGEN)));
        registerGameWithTests(PACMAN_XXL,       new PacManXXL_PacMan_GameModel(THE_GAME_BOX, xxlMapSelector, highScoreFile(PACMAN_XXL)));
        registerGameWithTests(MS_PACMAN_XXL,    new PacManXXL_MsPacMan_GameModel(THE_GAME_BOX, xxlMapSelector, highScoreFile(MS_PACMAN_XXL)));

        final var ui = new GameUI_Implementation(UI_CONFIG_MAP, THE_GAME_BOX, stage, sceneWidth, sceneHeight);

        ui.startPagesView().addStartPage(new ArcadePacMan_StartPage());
        ui.startPagesView().addStartPage(new ArcadeMsPacMan_StartPage());
        ui.startPagesView().addStartPage(new TengenMsPacMan_StartPage());
        ui.startPagesView().addStartPage(new PacManXXL_StartPage());

        ui.startPagesView().startPages().forEach(startPage -> startPage.init(ui));
        ui.startPagesView().setSelectedIndex(0);

        ui.dashboard().configure(List.of(DASHBOARD_IDS));
        ui.dashboard().addInfoBox(
            TengenMsPacMan_DashboardID.JOYPAD,
            TengenMsPacMan_UIConfig.TEXT_BUNDLE.getString("infobox.joypad.title"),
            new InfoBoxJoypad(ui));

        return ui;
    }

    private GameUI createUI_WithBuilder(
        Stage stage,
        double sceneWidth,
        double sceneHeight,
        PacManXXL_MapSelector xxlMapSelector)
    {
        Logger.info("Creating UI with builder");

        return GameUI_Builder.create(stage, sceneWidth, sceneHeight)
            .game(
                PACMAN.name(),
                ArcadePacMan_GameModel.class,
                ArcadePacMan_UIConfig.class)

            .game(
                MS_PACMAN.name(),
                ArcadeMsPacMan_GameModel.class,
                ArcadeMsPacMan_UIConfig.class)

            .game(
                MS_PACMAN_TENGEN.name(),
                TengenMsPacMan_GameModel.class,
                TengenMsPacMan_UIConfig.class)

            .game(
                PACMAN_XXL.name(),
                PacManXXL_PacMan_GameModel.class,
                xxlMapSelector,
                PacManXXL_PacMan_UIConfig.class)

            .game(
                MS_PACMAN_XXL.name(),
                PacManXXL_MsPacMan_GameModel.class,
                xxlMapSelector,
                PacManXXL_MsPacMan_UIConfig.class)

            .startPage(
                ArcadePacMan_StartPage.class,
                PACMAN.name())

            .startPage(
                ArcadeMsPacMan_StartPage.class,
                MS_PACMAN.name())

            .startPage(
                TengenMsPacMan_StartPage.class,
                MS_PACMAN_TENGEN.name())

            .startPage(
                PacManXXL_StartPage.class,
                PACMAN_XXL.name(), MS_PACMAN_XXL.name())

            .dashboard(DASHBOARD_IDS)

            .build();
    }

    private void addTestStates(Game game) {
        game.control().stateMachine().addState(new LevelShortTestState());
        game.control().stateMachine().addState(new LevelMediumTestState());
        game.control().stateMachine().addState(new CutScenesTestState());
    }

    private void registerGameWithTests(StandardGameVariant variant, Game game) {
        addTestStates(game);
        THE_GAME_BOX.registerGame(variant.name(), game);
    }

    private File highScoreFile(StandardGameVariant variant) {
        return THE_GAME_BOX.highScoreFile(variant.name());
    }
}