/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman.PacManXXL_MsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman_xxl.pacman.PacManXXL_PacMan_UIConfig;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameFlow;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.ui.app.AppConstants;
import de.amr.pacmanfx.ui.app.AppContext;
import de.amr.pacmanfx.ui.app.AppContextImpl;
import de.amr.pacmanfx.ui.app.AppBuilder;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.app.GameSpecification;
import de.amr.pacmanfx.ui.app.GamesContainer;
import de.amr.pacmanfx.ui.config.UIConfigManager;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.dashboard.Dashboard;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImpl;
import de.amr.pacmanfx.ui.view.GameViewMainScene;
import de.amr.pacmanfx.ui.view.StatusIconBox;
import de.amr.pacmanfx.uilib.GameClockFX;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariant.*;
import static de.amr.pacmanfx.core.Validations.requireNonNegative;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc.
 * ("All you can f** ähm play").
 * <p>
 * Command-line arguments:
 * <ul>
 *     <li>{@code --use_builder=value}:<br/>
 *     <li>{@code --include_tests=value}</li>
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

    private final CoinMechanism coinMechanism = new CoinMechanism(99);
    private GamesContainer gamesContainer;
    private AppContext app;
    private PacManXXL_MapSelector xxlMapSelector;

    private boolean useBuilder;
    private boolean includeTests;

    @Override
    public void init() {
        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
        gamesContainer = new GamesContainer();
        xxlMapSelector = new PacManXXL_MapSelector(gamesContainer.customMapDir());
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        try {
            if (useBuilder) {
                app = AppBuilder
                    .newApp(stage, sceneSize.x(), sceneSize.y(), gamesContainer, coinMechanism)

                    .game(
                        ARCADE_PACMAN,
                        Arcade_GameFlow::new,
                        ArcadePacMan_GameModel::new,
                        ArcadePacMan_GameRules::new,
                        ArcadePacMan_UIConfig::new
                    )

                    .game(
                        ARCADE_MS_PACMAN,
                        Arcade_GameFlow::new,
                        ArcadeMsPacMan_GameModel::new,
                        ArcadeMsPacMan_GameRules::new,
                        ArcadeMsPacMan_UIConfig::new
                    )

                    .game(
                        TENGEN_MS_PACMAN,
                        TengenMsPacMan_GameFlow::new,
                        TengenMsPacMan_GameModel::new,
                        TengenMsPacMan_GameRules::new,
                        TengenMsPacMan_UIConfig::new
                    )

                    .game(
                        ARCADE_PACMAN_XXL,
                        Arcade_GameFlow::new,
                        () -> new PacManXXL_PacMan_GameModel(xxlMapSelector),
                        PacManXXL_PacMan_GameRules::new,
                        PacManXXL_PacMan_UIConfig::new
                    )

                    .game(
                        ARCADE_MS_PACMAN_XXL,
                        Arcade_GameFlow::new,
                        () -> new PacManXXL_MsPacMan_GameModel(xxlMapSelector),
                        PacManXXL_MsPacMan_GameRules::new,
                        PacManXXL_MsPacMan_UIConfig::new
                    )

                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)

                    .interactiveTests(includeTests)

                    .build();
            }
            else {
                app = new AppContextImpl(
                    gamesContainer,
                    createView(stage, sceneSize.x(), sceneSize.y()),
                    new GameClockFX(),
                    coinMechanism);
                registerGames();
                addConfigFactories();
                addStartPages();
            }

            configureDashboard();
            Logger.info("UI created {} builder {} tests", using(useBuilder), including(includeTests));

            app.watchdog().addEventListener(xxlMapSelector);
            app.displayOnScreen();
        }
        catch (RuntimeException x) {
            Logger.error(x, "An error occurred starting the game.");
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        if (app != null) {
            app.terminate();
        }
    }

    // Private area

    private GameViewImpl createView(Stage stage, int width, int height) {
        return new GameViewImpl(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> AppConstants.LOCALIZED_TEXTS)
        );
    }

    private void registerGames() {
        for (GameVariant variant : GameVariant.values()) {
            final GameSpecification game = switch (variant) {

                case ARCADE_PACMAN -> new GameSpecification(
                    Arcade_GameFlow::new,
                    new ArcadePacMan_GameModel(),
                    new ArcadePacMan_GameRules()
                );

                case ARCADE_MS_PACMAN -> new GameSpecification(
                    Arcade_GameFlow::new,
                    new ArcadeMsPacMan_GameModel(),
                    new ArcadeMsPacMan_GameRules()
                );

                case TENGEN_MS_PACMAN -> new GameSpecification(
                    TengenMsPacMan_GameFlow::new,
                    new TengenMsPacMan_GameModel(),
                    new TengenMsPacMan_GameRules());

                case ARCADE_PACMAN_XXL -> new GameSpecification(
                    Arcade_GameFlow::new,
                    new PacManXXL_PacMan_GameModel(xxlMapSelector),
                    new PacManXXL_PacMan_GameRules()
                );

                case ARCADE_MS_PACMAN_XXL -> new GameSpecification(
                    Arcade_GameFlow::new,
                    new PacManXXL_MsPacMan_GameModel(xxlMapSelector),
                    new PacManXXL_MsPacMan_GameRules()
                );
            };

            //TODO
            /*
            if (includeTests) {
                addTestStates(game.gameModel().flow());
            }

             */
            gamesContainer.registerGame(variant.name(), game);
        }
    }

    private void addTestStates(GameFlow gameFlow) {
        gameFlow.addState(new LevelShortTestState());
        gameFlow.addState(new LevelMediumTestState());
        gameFlow.addState(new CutScenesTestState());
    }

    private void addConfigFactories() {
        final UIConfigManager configManager = app.ui().configurations();
        configManager.addConfigFactory(ARCADE_PACMAN.name(),        ArcadePacMan_UIConfig::new);
        configManager.addConfigFactory(ARCADE_MS_PACMAN.name(),     ArcadeMsPacMan_UIConfig::new);
        configManager.addConfigFactory(TENGEN_MS_PACMAN.name(),     TengenMsPacMan_UIConfig::new);
        configManager.addConfigFactory(ARCADE_PACMAN_XXL.name(),    PacManXXL_PacMan_UIConfig::new);
        configManager.addConfigFactory(ARCADE_MS_PACMAN_XXL.name(), PacManXXL_MsPacMan_UIConfig::new);

    }

    private void addStartPages() {
        final StartPagesView startView = app.ui().subViews().startView();
        startView.addStartPage(new ArcadePacMan_StartPage());
        startView.addStartPage(new ArcadeMsPacMan_StartPage());
        startView.addStartPage(new TengenMsPacMan_StartPage());
        startView.addStartPage(new PacManXXL_StartPage());
        startView.startPages().forEach(startPage -> startPage.init(app));
        startView.setSelectedIndex(0);
    }


    private void configureDashboard() {
        final Dashboard dashboard = app.ui().subViews().gamePlayView().dashboard();

        app.ui().subViews().gamePlayView().configureDashboard(DASHBOARD_IDs, app.ui().translations());

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
                section.setCustomDirWatchDog(app.watchdog());
                section.setMapEditFunction(mapFile -> CommonActions.editMapFile(app, mapFile));
            });
    }
}