/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.appsuite;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacManGameConfig_3D;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.games.pacman.arcade.ms_pacman.MsPacManGame;
import de.amr.games.pacman.arcade.pacman.ArcadePacManGameConfig_3D;
import de.amr.games.pacman.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.games.pacman.arcade.pacman.PacManGame;
import de.amr.games.pacman.arcade.pacman_xxl.ArcadePacManXXLGameConfig_3D;
import de.amr.games.pacman.arcade.pacman_xxl.ArcadePacManXXL_StartPage;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXL;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfig_3D;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_StartPage;
import de.amr.games.pacman.ui2d.DashboardItemID;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import de.amr.games.pacman.ui3d.model.Model3D;
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

import java.io.File;
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

    @Override
    public void init() {
        File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
        if (userDir.mkdir()) {
            Logger.info("User dir '{}' created", userDir);
        }
        GameController.it().addGameImplementation(GameVariant.MS_PACMAN, new MsPacManGame(userDir));
        GameController.it().addGameImplementation(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengen(userDir));
        GameController.it().addGameImplementation(GameVariant.PACMAN, new PacManGame(userDir));
        GameController.it().addGameImplementation(GameVariant.PACMAN_XXL, new PacManGameXXL(userDir));
        GameController.it().selectGame(GameVariant.PACMAN);
        GlobalProperties3d.PY_3D_ENABLED.set(false);
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGamesUI_3D();
        ui.loadAssets();

        // UI asset storage exists now, add game variants including their own assets
        ui.configureGameVariant(GameVariant.MS_PACMAN, new ArcadeMsPacManGameConfig_3D(ui.assets()));
        ui.configureGameVariant(GameVariant.MS_PACMAN_TENGEN, new TengenMsPacMan_GameConfig_3D(ui.assets()));
        ui.configureGameVariant(GameVariant.PACMAN, new ArcadePacManGameConfig_3D(ui.assets()));
        ui.configureGameVariant(GameVariant.PACMAN_XXL, new ArcadePacManXXLGameConfig_3D(ui.assets()));

        ui.create(stage, initialSize());
        ui.startPage().addSlide(new ArcadePacMan_StartPage().root());
        ui.startPage().addSlide(new ArcadeMsPacMan_StartPage().root());
        ui.startPage().addSlide(new ArcadePacManXXL_StartPage().root());
        ui.startPage().addSlide(new TengenMsPacMan_StartPage().root());

        ui.addDashboardItem(DashboardItemID.README);
        ui.addDashboardItem(DashboardItemID.GENERAL);
        ui.addDashboardItem(DashboardItemID.GAME_CONTROL);
        ui.addDashboardItem(ui.locText("infobox.3D_settings.title"), new InfoBox3D());
        ui.addDashboardItem(DashboardItemID.GAME_INFO);
        ui.addDashboardItem(DashboardItemID.ACTOR_INFO);
        ui.addDashboardItem(ui.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        ui.addDashboardItem(DashboardItemID.JOYPAD);
        ui.addDashboardItem(DashboardItemID.KEYBOARD);
        ui.addDashboardItem(DashboardItemID.ABOUT);

        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets: {}", ui.assets().summary(Map.of(
            Model3D.class,"3D models",
            Image.class, "images",
            Font.class, "fonts",
            Color.class, "colors",
            AudioClip.class, "audio clips")));

        ui.show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.stop();
    }
}