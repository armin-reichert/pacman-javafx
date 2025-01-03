/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.appsuite;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacManStartPage;
import de.amr.games.pacman.arcade.ms_pacman.MsPacManGame;
import de.amr.games.pacman.arcade.ms_pacman.MsPacManGameConfiguration;
import de.amr.games.pacman.arcade.pacman.ArcadePacManStartPage;
import de.amr.games.pacman.arcade.pacman.PacManGame;
import de.amr.games.pacman.arcade.pacman.PacManGameConfiguration;
import de.amr.games.pacman.arcade.pacman_xxl.ArcadePacManXXLStartPage;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXL;
import de.amr.games.pacman.arcade.pacman_xxl.PacManGameXXLConfiguration;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacManStartPage;
import de.amr.games.pacman.ui2d.DashboardItemID;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
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
 * Application containing all game variants and including play scenes in 2D only.
 *
 * @author Armin Reichert
 */
public class PacManGames2dApp extends Application {

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }

    private PacManGamesUI ui;

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
    }

    @Override
    public void start(Stage stage) {
        ui = new PacManGamesUI();
        ui.loadAssets();

        // UI asset storage exists now, add game variants including their own assets
        ui.configureGameVariant(GameVariant.PACMAN, new PacManGameConfiguration(ui.assets()));
        ui.configureGameVariant(GameVariant.MS_PACMAN, new MsPacManGameConfiguration(ui.assets()));
        ui.configureGameVariant(GameVariant.PACMAN_XXL, new PacManGameXXLConfiguration(ui.assets()));
        ui.configureGameVariant(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengenConfiguration(ui.assets()));

        ui.create(stage, initialSize());
        ui.startPage().addSlide(new ArcadePacManStartPage().root());
        ui.startPage().addSlide(new ArcadeMsPacManStartPage().root());
        ui.startPage().addSlide(new ArcadePacManXXLStartPage().root());
        ui.startPage().addSlide(new TengenMsPacManStartPage().root());

        ui.addDashboardItem(DashboardItemID.README);
        ui.addDashboardItem(DashboardItemID.GENERAL);
        ui.addDashboardItem(DashboardItemID.GAME_CONTROL);
        ui.addDashboardItem(DashboardItemID.GAME_INFO);
        ui.addDashboardItem(DashboardItemID.ACTOR_INFO);
        ui.addDashboardItem(ui.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        ui.addDashboardItem(DashboardItemID.JOYPAD);
        ui.addDashboardItem(DashboardItemID.KEYBOARD);
        ui.addDashboardItem(DashboardItemID.ABOUT);

        Logger.info("JavaFX version: {}", System.getProperty("javafx.runtime.version"));
        Logger.info("Assets loaded: {}", ui.assets().summary(
            Map.of(Image.class, "images",  Font.class, "fonts", Color.class, "colors", AudioClip.class, "audio clips")
        ));

        ui.show();
        Logger.info("Application started. Stage size: {0} x {0} px", stage.getWidth(), stage.getHeight());
    }

    @Override
    public void stop() {
        ui.stop();
    }
}