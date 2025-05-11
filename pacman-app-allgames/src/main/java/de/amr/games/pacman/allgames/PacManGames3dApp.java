/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.allgames;

import de.amr.games.pacman.arcade.ArcadePacMan_GameModel;
import de.amr.games.pacman.arcade.ArcadePacMan_StartPage;
import de.amr.games.pacman.arcade.ArcadePacMan_UIConfig;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.games.pacman.arcade.pacman_xxl.*;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.games.pacman.ui.dashboard.DashboardID;
import de.amr.games.pacman.ui.dashboard.InfoBoxCustomMaps;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.ui.Globals.*;

/**
 * Application containing all game variants and including 3D play scene.
 *
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private XXLAnyPacMan_MapSelector xxlMapSelector;

    @Override
    public void init() {
        checkUserDirsExistAndWritable();
        xxlMapSelector = new XXLAnyPacMan_MapSelector(CUSTOM_MAP_DIR);
        THE_GAME_CONTROLLER.register(GameVariant.MS_PACMAN,        new ArcadeMsPacMan_GameModel());
        THE_GAME_CONTROLLER.register(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
        THE_GAME_CONTROLLER.register(GameVariant.PACMAN,           new ArcadePacMan_GameModel());
        THE_GAME_CONTROLLER.register(GameVariant.PACMAN_XXL,       new XXLPacMan_GameModel(xxlMapSelector));
        THE_GAME_CONTROLLER.register(GameVariant.MS_PACMAN_XXL,    new XXLMsPacMan_GameModel(xxlMapSelector));
        THE_GAME_CONTROLLER.select(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double height = 0.8 * screenSize.getHeight(), width = 1.6 * height;
        createUI(Map.of(
            GameVariant.PACMAN,           ArcadePacMan_UIConfig.class,
            GameVariant.MS_PACMAN,        ArcadeMsPacMan_UIConfig.class,
            GameVariant.MS_PACMAN_TENGEN, TengenMsPacMan_UIConfig.class,
            GameVariant.PACMAN_XXL,       XXLPacMan_UIConfig.class,
            GameVariant.MS_PACMAN_XXL,    XXLMsPacMan_UIConfig.class
        ));
        THE_UI.build(stage, width, height);
        THE_UI.buildDashboard(
                DashboardID.README,
                DashboardID.GENERAL,
                DashboardID.GAME_CONTROL,
                DashboardID.SETTINGS_3D,
                DashboardID.GAME_INFO,
                DashboardID.ACTOR_INFO,
                DashboardID.CUSTOM_MAPS,
                DashboardID.KEYBOARD,
                DashboardID.ABOUT);

        InfoBoxCustomMaps infoBoxCustomMaps = THE_UI.dashboard().getInfoBox(DashboardID.CUSTOM_MAPS);
        infoBoxCustomMaps.setTableItems(xxlMapSelector.customMaps());
        xxlMapSelector.startWatchingCustomMaps();

        THE_UI.addStartPage(new ArcadePacMan_StartPage(GameVariant.PACMAN));
        THE_UI.addStartPage(new ArcadeMsPacMan_StartPage(GameVariant.MS_PACMAN));
        THE_UI.addStartPage(new TengenMsPacMan_StartPage(GameVariant.MS_PACMAN_TENGEN));
        THE_UI.addStartPage(new XXLAnyPacMan_StartPage());
        THE_UI.selectStartPage(0);
        THE_UI.show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        THE_CLOCK.stop();
    }
}