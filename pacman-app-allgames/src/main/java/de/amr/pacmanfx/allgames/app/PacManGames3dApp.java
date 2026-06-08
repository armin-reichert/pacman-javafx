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
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameFlow;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameRules;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.game.*;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.dashboard.Dashboard;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImplementation;
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

    private GamesCollection gamesCollection;
    private Game game;

    private boolean useBuilder;
    private boolean includeTests;

    @Override
    public void init() {
        gamesCollection = new GamesCollection();
        fillGamesCollection(gamesCollection);

        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();

        try {
            if (useBuilder) {
                game = GameBuilder.compose(gamesCollection, stage, sceneSize.x(), sceneSize.y())

                    .gameVariant(ARCADE_PACMAN.name(), true)
                    .gameVariant(ARCADE_MS_PACMAN.name(), true)
                    .gameVariant(TENGEN_MS_PACMAN.name(), true)
                    .gameVariant(ARCADE_PACMAN_XXL.name(), xxlMapSelector, true)
                    .gameVariant(ARCADE_MS_PACMAN_XXL.name(), xxlMapSelector, true)

                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)

                    .coinMechanism(true)
                    .build();
            }
            else {
                game = new GameImplementation(
                    gamesCollection,
                    createView(stage, sceneSize.x(), sceneSize.y()),
                    new GameClockFX(),
                    new CoinMechanism());

                game.gameVariantImpl(GameVariant.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(xxlMapSelector);
                game.gameVariantImpl(GameVariant.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(xxlMapSelector);
                addStartPages();
            }

            configureDashboard();
            game.watchdog().addEventListener(xxlMapSelector); //TODO

            Logger.info("UI created {} builder {} tests", using(useBuilder), including(includeTests));
        }
        catch (RuntimeException x) {
            Logger.error(x, "An error occurred starting the game.");
            Platform.exit();
        }

        game.displayOnScreen();
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }

    // Private area

    private GameViewImplementation createView(Stage stage, int width, int height) {
        return new GameViewImplementation(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> GameConstants.LOCALIZED_TEXTS)
        );
    }

    private void fillGamesCollection(GamesCollection gamesCollection) {
        for (GameVariant variant : GameVariant.values()) {
            final GameVariantSpecification game = switch (variant) {

                case ARCADE_PACMAN -> new GameVariantSpecification(
                    Arcade_GameFlow::new,
                    ArcadePacMan_GameModel::new,
                    ArcadePacMan_GameRules::new,
                    ArcadePacMan_UIConfig::new
                );

                case ARCADE_MS_PACMAN -> new GameVariantSpecification(
                    Arcade_GameFlow::new,
                    ArcadeMsPacMan_GameModel::new,
                    ArcadeMsPacMan_GameRules::new,
                    ArcadeMsPacMan_UIConfig::new
                );

                case TENGEN_MS_PACMAN -> new GameVariantSpecification(
                    TengenMsPacMan_GameFlow::new,
                    TengenMsPacMan_GameModel::new,
                    TengenMsPacMan_GameRules::new,
                    TengenMsPacMan_UIConfig::new
                );

                case ARCADE_PACMAN_XXL -> new GameVariantSpecification(
                    Arcade_GameFlow::new,
                    PacManXXL_PacMan_GameModel::new,
                    PacManXXL_PacMan_GameRules::new,
                    PacManXXL_PacMan_UIConfig::new
                );

                case ARCADE_MS_PACMAN_XXL -> new GameVariantSpecification(
                    Arcade_GameFlow::new,
                    PacManXXL_MsPacMan_GameModel::new,
                    PacManXXL_MsPacMan_GameRules::new,
                    PacManXXL_MsPacMan_UIConfig::new
                );
            };
            gamesCollection.registerGame(variant.name(), game);
        }
    }

    private void addStartPages() {
        final StartPagesView startView = game.ui().subViews().startView();
        startView.addStartPage(new ArcadePacMan_StartPage());
        startView.addStartPage(new ArcadeMsPacMan_StartPage());
        startView.addStartPage(new TengenMsPacMan_StartPage());
        startView.addStartPage(new PacManXXL_StartPage());
        startView.startPages().forEach(startPage -> startPage.init(game));
        startView.setSelectedIndex(0);
    }

    private void configureDashboard() {
        final Dashboard dashboard = game.ui().subViews().gamePlayView().dashboard();

        game.ui().subViews().gamePlayView().configureDashboard(DASHBOARD_IDs, game.ui().translations());

        // Add Joypad controller section
        dashboard.addSection(
            TengenMsPacMan_DashboardID.JOYPAD,
            new DashboardSectionJoypad(dashboard),
            game.gameVariantImpl(TENGEN_MS_PACMAN.name()).uiConfig().translate("infobox.joypad.title"),
            false);

        // Configure custom map section table
        dashboard.findSection(CommonDashboardID.CUSTOM_MAPS)
            .filter(DashboardSectionCustomMaps.class::isInstance)
            .map(DashboardSectionCustomMaps.class::cast)
            .ifPresent(section -> {
                section.setCustomDirWatchDog(game.watchdog());
                section.setMapEditFunction(mapFile -> CommonActions.editMapFile(game, mapFile));
            });
    }
}