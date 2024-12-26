/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.apps;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration_3D;
import de.amr.games.pacman.tengen.ms_pacman.TengenMsPacManStartPage;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxJoypad;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

import static de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration.NES_SIZE;

public class TengenMsPacManApp extends Application {

    @Override
    public void init() {
        try {
            File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
            if (userDir.mkdir()) {
                Logger.info("User dir '{}' created", userDir);
            }
            GameController.it().addGameImplementation(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengen(userDir));
            GameController.it().selectGame(GameVariant.MS_PACMAN_TENGEN);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.loadAssets();
        ui.configureGameVariant(GameVariant.MS_PACMAN_TENGEN, new MsPacManGameTengenConfiguration_3D(ui.assets()));
        ui.create(stage, initialSize());
        ui.startPage().addSlide(new TengenMsPacManStartPage().root());

        ui.addDashboardItem(PacManGamesUI.DashboardItemID.README);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.GENERAL);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.GAME_CONTROL);
        ui.addDashboardItem(ui.locText("infobox.3D_settings.title"), new InfoBox3D());
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.GAME_INFO);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.ACTOR_INFO);
        ui.addDashboardItem("Joypad Settings", new InfoBoxJoypad());
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.KEYBOARD);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.ABOUT);
        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = (double) NES_SIZE.x() / NES_SIZE.y();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
