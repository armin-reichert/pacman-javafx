/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.allgames.app;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman_xxl.*;
import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.model.StandardGameVariant;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc. ("all you can f*** ähm play").
 */
public class PacManGames3dApp extends Application {

    private static final float ASPECT_RATIO = 1.6f; // 16:10 aspect ratio
    private static final float USED_HEIGHT = 0.8f;  // 80% of available height

    private GameUI ui;

    @Override
    public void start(Stage primaryStage) {
        try {
            final int height = (int) Math.round(USED_HEIGHT * Screen.getPrimary().getBounds().getHeight());
            final int width  = Math.round(ASPECT_RATIO * height);

            //TODO create this by reflection inside builder too?
            final var mapSelectorXXL = new PacManXXL_Common_MapSelector(GameBox.CUSTOM_MAP_DIR);

            ui = GameUI_Builder.create(primaryStage, width, height)

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
                    mapSelectorXXL,
                    PacManXXL_PacMan_UIConfig.class)

                .game(
                    StandardGameVariant.MS_PACMAN_XXL.name(),
                    PacManXXL_MsPacMan_GameModel.class,
                    mapSelectorXXL,
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
                    DashboardID.KEYBOARD_SHORTCUTS_GLOBAL,
                    DashboardID.KEYBOARD_SHORTCUTS_LOCAL,
                    DashboardID.ABOUT)

                .build();

            ui.directoryWatchdog().addEventListener(watchEvents -> {
                if (!watchEvents.isEmpty()) {
                    mapSelectorXXL.customMapPrototypes().clear();
                    mapSelectorXXL.loadCustomMapPrototypes();
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