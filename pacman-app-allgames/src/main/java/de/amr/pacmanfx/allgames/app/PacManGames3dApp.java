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
import de.amr.pacmanfx.lib.fsm.StateMachine;
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
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.dashboard.DashboardSectionCustomMaps;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

import static de.amr.pacmanfx.GameContext.highScoreFile;
import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static de.amr.pacmanfx.model.GameVariant.*;

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

    private static final float ASPECT_RATIO    = 1.6f; // 16:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

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

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        final Dimension2D sceneSize = Ufx.computeSceneSize(ASPECT_RATIO, HEIGHT_FRACTION);
        try {
            final boolean useBuilder = Boolean.parseBoolean(getParameters().getNamed().getOrDefault("use_builder", "true"));
            // Shared map selector used by Pac-Man XXL and Ms. Pac-Man XXL
            final var xxlMapSelector = new PacManXXL_MapSelector(GameBox.CUSTOM_MAP_DIR);
            if (useBuilder) {
                ui = GameUI_Builder
                    .newUI(primaryStage, sceneSize.getWidth(), sceneSize.getHeight())

                    .game(ARCADE_PACMAN,
                        () -> new ArcadePacMan_GameModel(THE_GAME_BOX, highScoreFile(ARCADE_PACMAN)),
                        ArcadePacMan_UIConfig::new)

                    .game(ARCADE_MS_PACMAN,
                        () ->new ArcadeMsPacMan_GameModel(THE_GAME_BOX, highScoreFile(ARCADE_MS_PACMAN)),
                        ArcadeMsPacMan_UIConfig::new)

                    .game(TENGEN_MS_PACMAN,
                        () -> new TengenMsPacMan_GameModel(highScoreFile(TENGEN_MS_PACMAN)),
                        TengenMsPacMan_UIConfig::new)

                    .game(ARCADE_PACMAN_XXL,
                        () -> new PacManXXL_PacMan_GameModel(THE_GAME_BOX, xxlMapSelector, highScoreFile(ARCADE_PACMAN_XXL)),
                        PacManXXL_PacMan_UIConfig::new)

                    .game(ARCADE_MS_PACMAN_XXL,
                        () -> new PacManXXL_MsPacMan_GameModel(THE_GAME_BOX, xxlMapSelector, highScoreFile(ARCADE_MS_PACMAN_XXL)),
                        PacManXXL_MsPacMan_UIConfig::new)

                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)
                    .dashboard(DASHBOARD_IDs.toArray(CommonDashboardID[]::new))
                    .build();
            }
            else {
                for (GameVariant variant : GameVariant.values()) {
                    registerGameWithTestStates(variant, xxlMapSelector);
                }
                ui = new GameUI_Implementation(THE_GAME_BOX, primaryStage, sceneSize.getWidth(), sceneSize.getHeight());

                ui.uiConfigManager().addFactory(ARCADE_PACMAN.name(),        ArcadePacMan_UIConfig::new);
                ui.uiConfigManager().addFactory(ARCADE_MS_PACMAN.name(),     ArcadeMsPacMan_UIConfig::new);
                ui.uiConfigManager().addFactory(TENGEN_MS_PACMAN.name(),     TengenMsPacMan_UIConfig::new);
                ui.uiConfigManager().addFactory(ARCADE_PACMAN_XXL.name(),    PacManXXL_PacMan_UIConfig::new);
                ui.uiConfigManager().addFactory(ARCADE_MS_PACMAN_XXL.name(), PacManXXL_MsPacMan_UIConfig::new);

                final StartPagesCarousel startPages = ui.views().startPagesView();
                startPages.addStartPage(new ArcadePacMan_StartPage());
                startPages.addStartPage(new ArcadeMsPacMan_StartPage());
                startPages.addStartPage(new TengenMsPacMan_StartPage());
                startPages.addStartPage(new PacManXXL_StartPage());
                startPages.startPages().forEach(startPage -> startPage.init(ui));
                startPages.setSelectedIndex(0);

                final Dashboard dashboard = ui.views().playView().dashboard();
                dashboard.addCommonSections(ui, DASHBOARD_IDs);
            }
            configureDashboard();
            Logger.info("UI created {} builder", useBuilder ? "using" : "without");
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

    private void registerGameWithTestStates(GameVariant gameVariant, PacManXXL_MapSelector xxlMapSelector) {
        final File highScoreFile = highScoreFile(gameVariant);
        final Game game = switch (gameVariant) {
            case ARCADE_PACMAN -> new ArcadePacMan_GameModel(THE_GAME_BOX, highScoreFile);
            case ARCADE_MS_PACMAN -> new ArcadeMsPacMan_GameModel(THE_GAME_BOX, highScoreFile);
            case TENGEN_MS_PACMAN -> new TengenMsPacMan_GameModel(highScoreFile);
            case ARCADE_PACMAN_XXL -> new PacManXXL_PacMan_GameModel(THE_GAME_BOX, xxlMapSelector, highScoreFile);
            case ARCADE_MS_PACMAN_XXL -> new PacManXXL_MsPacMan_GameModel(THE_GAME_BOX, xxlMapSelector, highScoreFile);
        };
        final StateMachine<Game> gameStateMachine = game.control().stateMachine();
        gameStateMachine.addState(new LevelShortTestState());
        gameStateMachine.addState(new LevelMediumTestState());
        gameStateMachine.addState(new CutScenesTestState());
        THE_GAME_BOX.registerGame(gameVariant.name(), game);
    }

    private void configureDashboard() {
        final Dashboard dashboard = ui.views().playView().dashboard();

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
                section.setMapEditFunction(mapFile -> ui.editWorldMap(mapFile));
            });
    }
}