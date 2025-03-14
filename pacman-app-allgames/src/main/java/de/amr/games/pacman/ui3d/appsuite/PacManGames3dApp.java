/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.appsuite;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.games.pacman.arcade.pacman.ArcadePacMan_GameModel;
import de.amr.games.pacman.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.games.pacman.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.games.pacman.arcade.pacman_xxl.*;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.DirectoryWatchdog;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameModel;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.games.pacman.ui._2d.StartPage;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.uilib.model3D.Model3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.Map;

/**
 * Application containing all game variants and including 3D play scene.
 *
 * @author Armin Reichert
 */
public class PacManGames3dApp extends Application {

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.6;
        double height = 0.8 * screenSize.getHeight();
        double width = aspect * height;
        return new Dimension2D(width, height);
    }

    private PacManGamesUI_3D ui;
    private final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();

    @Override
    public void init() {
        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        GameController gameController = GameController.it();
        gameController.setGameModel(GameVariant.MS_PACMAN,        new ArcadeMsPacMan_GameModel());
        gameController.setGameModel(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameModel());
        gameController.setGameModel(GameVariant.PACMAN,           new ArcadePacMan_GameModel());
        gameController.setGameModel(GameVariant.PACMAN_XXL,       new PacManXXL_PacMan_GameModel(xxlMapSelector));
        gameController.setGameModel(GameVariant.MS_PACMAN_XXL,    new PacManXXL_MsPacMan_GameModel(xxlMapSelector));
        gameController.gameModels().forEach(GameModel::init);
        gameController.selectGame(GameVariant.PACMAN);
    }

    @Override
    public void start(Stage stage) throws IOException {
        ui = new PacManGamesUI_3D();
        ui.configure(GameVariant.PACMAN,           new ArcadePacMan_UIConfig(ui.assets()));
        ui.configure(GameVariant.MS_PACMAN,        new ArcadeMsPacMan_UIConfig(ui.assets()));
        ui.configure(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_UIConfig(ui.assets()));
        ui.configure(GameVariant.PACMAN_XXL,       new PacManXXL_PacMan_UIConfig(ui.assets()));
        ui.configure(GameVariant.MS_PACMAN_XXL,    new PacManXXL_MsPacMan_UIConfig(ui.assets()));

        ui.create(stage, initialSize());

        ui.gameView().addDefaultDashboardItems(
            "README",
            "GENERAL",
            "GAME_CONTROL",
            "SETTINGS_3D",
            "GAME_INFO",
            "ACTOR_INFO",
            "CUSTOM_MAPS",
            "JOYPAD",
            "KEYBOARD",
            "ABOUT"
        );

        InfoBoxCustomMaps infoBoxCustomMaps = ui.getDashboardItem("CUSTOM_MAPS");
        infoBoxCustomMaps.setTableItems(xxlMapSelector.customMaps());

        ui.startPageSelectionView().addStartPage(GameVariant.PACMAN,           new ArcadePacMan_StartPage(ui));
        ui.startPageSelectionView().addStartPage(GameVariant.MS_PACMAN,        new ArcadeMsPacMan_StartPage(ui));
        ui.startPageSelectionView().addStartPage(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_StartPage(ui));

        StartPage xxlStartPage = new PacManXXL_StartPage(ui);
        ui.startPageSelectionView().addStartPage(GameVariant.PACMAN_XXL,    xxlStartPage);
        ui.startPageSelectionView().addStartPage(GameVariant.MS_PACMAN_XXL, xxlStartPage);

        DirectoryWatchdog goodBoy = new DirectoryWatchdog(GameModel.CUSTOM_MAP_DIR);
        goodBoy.setEventConsumer(eventList -> {
            Logger.info("Custom map change detected, reload custom maps...");
            xxlMapSelector.setCustomMapsUpToDate(false);
            xxlMapSelector.loadCustomMaps();
        });
        goodBoy.startWatching();

        stage.show();

        Logger.info("Assets: {}", ui.assets().summary(Map.of(
                Model3D.class,"3D models",
                Image.class, "images",
                Font.class, "fonts",
                Color.class, "colors",
                AudioClip.class, "audio clips")));

        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.gameClock().stop();
    }
}