/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.ConfigurationsManager;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.uilib.GameClockFX;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.model.GameVariant.*;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc.
 * ("All you can f** ähm play").
 * <p>
 * Command-line arguments:
 * <ul>
 *     <li>{@code --use_builder=value}:<br/>
 *     <li>{@code --include-tests=value}</li>
 * </ul>
 * </p>
 */
public class PacManGames3dApp extends Application {

    private static final float ASPECT_RATIO    = 1.6f; // 16:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private static String including(boolean b) {
        return b ? "including" : "not including";
    }

    private static String using(boolean b) {
        return b ? "using" : "without using";
    }

    private static final List<CommonDashboardID> DASHBOARD_IDs = List.of(
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
    );

    private GameBox gameBox;
    private GameUI ui;
    private PacManXXL_MapSelector xxlMapSelector;

    private boolean useBuilder;
    private boolean includeTests;

    @Override
    public void init() {
        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
        gameBox = new GameBox(new GameClockFX(), new CoinMechanism(99));
        xxlMapSelector = new PacManXXL_MapSelector(gameBox.customMapDir());
        registerGames();
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final CoinMechanism coinMechanism = gameBox.coinMechanism();
        try {
            if (useBuilder) {
                ui = GameUI_Builder
                    .newUI(primaryStage, sceneSize.x(), sceneSize.y(), gameBox)

                    .game(ARCADE_PACMAN,
                        () -> new ArcadePacMan_GameModel(coinMechanism),
                        ArcadePacMan_UIConfig::new)

                    .game(ARCADE_MS_PACMAN,
                        () -> new ArcadeMsPacMan_GameModel(coinMechanism),
                        ArcadeMsPacMan_UIConfig::new)

                    .game(TENGEN_MS_PACMAN,
                        TengenMsPacMan_GameModel::new,
                        TengenMsPacMan_UIConfig::new)

                    .game(ARCADE_PACMAN_XXL,
                        () -> new PacManXXL_PacMan_GameModel(coinMechanism, xxlMapSelector),
                        PacManXXL_PacMan_UIConfig::new)

                    .game(ARCADE_MS_PACMAN_XXL,
                        () -> new PacManXXL_MsPacMan_GameModel(coinMechanism, xxlMapSelector),
                        PacManXXL_MsPacMan_UIConfig::new)

                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)

                    .includeInteractiveTests(includeTests)

                    .build();
            }
            else {
                ui = new GameUI_Implementation(gameBox, primaryStage, sceneSize.x(), sceneSize.y());
                addConfigFactories();
                addStartPages();
            }

            configureDashboard();
            Logger.info("UI created {} builder {} tests", using(useBuilder), including(includeTests));

            ui.customDirWatchdog().addEventListener(xxlMapSelector);
            ui.show();
        }
        catch (RuntimeException x) {
            Logger.error(x, "An error occurred starting the game.");
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (ui != null) {
            ui.terminate();
        }
    }

    // Private area

    private void registerGames() {
        for (GameVariant variant : GameVariant.values()) {
            final AbstractGameModel game = switch (variant) {
                case ARCADE_PACMAN        -> new ArcadePacMan_GameModel(gameBox.coinMechanism());
                case ARCADE_MS_PACMAN     -> new ArcadeMsPacMan_GameModel(gameBox.coinMechanism());
                case TENGEN_MS_PACMAN     -> new TengenMsPacMan_GameModel();
                case ARCADE_PACMAN_XXL    -> new PacManXXL_PacMan_GameModel(gameBox.coinMechanism(), xxlMapSelector);
                case ARCADE_MS_PACMAN_XXL -> new PacManXXL_MsPacMan_GameModel(gameBox.coinMechanism(), xxlMapSelector);
            };
            if (includeTests) {
                addTestStates(game);
            }
            gameBox.registerGame(variant.name(), game);
        }
    }

    private void addTestStates(Game game) {
        game.flow().addState(new LevelShortTestState<>(gameBox.coinMechanism()));
        game.flow().addState(new LevelMediumTestState<>());
        game.flow().addState(new CutScenesTestState<>());
    }

    private void addConfigFactories() {
        final ConfigurationsManager configManager = ui.services().configurations();
        configManager.addConfigFactory(ARCADE_PACMAN.name(),        ArcadePacMan_UIConfig::new);
        configManager.addConfigFactory(ARCADE_MS_PACMAN.name(),     ArcadeMsPacMan_UIConfig::new);
        configManager.addConfigFactory(TENGEN_MS_PACMAN.name(),     TengenMsPacMan_UIConfig::new);
        configManager.addConfigFactory(ARCADE_PACMAN_XXL.name(),    PacManXXL_PacMan_UIConfig::new);
        configManager.addConfigFactory(ARCADE_MS_PACMAN_XXL.name(), PacManXXL_MsPacMan_UIConfig::new);

    }

    private void addStartPages() {
        final StartPagesCarousel startView = ui.services().views().startView();
        startView.addStartPage(new ArcadePacMan_StartPage());
        startView.addStartPage(new ArcadeMsPacMan_StartPage());
        startView.addStartPage(new TengenMsPacMan_StartPage());
        startView.addStartPage(new PacManXXL_StartPage());
        startView.startPages().forEach(startPage -> startPage.init(ui));
        startView.setSelectedIndex(0);
    }


    private void configureDashboard() {
        final Dashboard dashboard = ui.services().views().playView().dashboard();

        ui.services().configureDashboard(DASHBOARD_IDs);

        // Add Joypad controller section
        dashboard.addSection(
            TengenMsPacMan_DashboardID.JOYPAD,
            new DashboardSectionJoypad(dashboard),
            TengenMsPacMan_UIConfig.TEXT_BUNDLE.getString("infobox.joypad.title"),
            false);

        // Configure custom map section table
        dashboard.findSection(CommonDashboardID.CUSTOM_MAPS)
            .filter(DashboardSectionCustomMaps.class::isInstance)
            .map(DashboardSectionCustomMaps.class::cast)
            .ifPresent(section -> {
                section.setCustomDirWatchDog(ui.customDirWatchdog());
                section.setMapEditFunction(mapFile -> ui.openWorldMapFileInEditor(mapFile));
            });
    }
}