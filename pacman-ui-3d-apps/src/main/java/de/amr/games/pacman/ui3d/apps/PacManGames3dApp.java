/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.apps;

import de.amr.games.pacman.arcade.ms_pacman.MsPacManGame;
import de.amr.games.pacman.arcade.pacman.PacManGame;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXL;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui3d.GlobalProperties3d;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import de.amr.games.pacman.ui3d.model.Model3D;
import de.amr.games.pacman.ui3d.variants.MsPacManGameConfiguration_3D;
import de.amr.games.pacman.ui3d.variants.MsPacManGameTengenConfiguration_3D;
import de.amr.games.pacman.ui3d.variants.PacManGameConfiguration_3D;
import de.amr.games.pacman.ui3d.variants.PacManGameXXLConfiguration_3D;
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
        ui.setGameConfiguration(GameVariant.MS_PACMAN, new MsPacManGameConfiguration_3D());
        ui.setGameConfiguration(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengenConfiguration_3D());
        ui.setGameConfiguration(GameVariant.PACMAN, new PacManGameConfiguration_3D());
        ui.setGameConfiguration(GameVariant.PACMAN_XXL, new PacManGameXXLConfiguration_3D());
        for (GameVariant variant : GameVariant.values()) {
            ui.assets().addAll(ui.gameConfiguration(variant).assets());
        }
        ui.create(stage, initialSize());
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.README);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.GENERAL);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.GAME_CONTROL);
        ui.appendDashboardItem(ui.locText("infobox.3D_settings.title"), new InfoBox3D());
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.GAME_INFO);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.ACTOR_INFO);
        ui.appendDashboardItem(ui.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.KEYBOARD);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.ABOUT);

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