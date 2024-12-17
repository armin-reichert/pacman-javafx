/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui3d.apps;

import de.amr.games.pacman.arcade.pacman.ArcadePacManStartPage;
import de.amr.games.pacman.arcade.pacman.PacManGame;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import de.amr.games.pacman.ui3d.variants.PacManGameConfiguration_3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ArcadePacManApp extends Application {

    @Override
    public void init() {
        try {
            File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
            if (userDir.mkdir()) {
                Logger.info("User dir '{}' created", userDir);
            }
            GameController.it().addGameImplementation(GameVariant.PACMAN, new PacManGame(userDir));
            GameController.it().selectGame(GameVariant.PACMAN);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.loadAssets();
        ui.setGameConfiguration(GameVariant.PACMAN, new PacManGameConfiguration_3D());
        ui.create(stage, initialSize());
        ui.addStartPageCarouselSlide(new ArcadePacManStartPage().root());

        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.README);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.GENERAL);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.GAME_CONTROL);
        ui.appendDashboardItem(ui.locText("infobox.3D_settings.title"), new InfoBox3D());
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.GAME_INFO);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.ACTOR_INFO);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.KEYBOARD);
        ui.appendDashboardItem(PacManGamesUI.DashboardItemID.ABOUT);
        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
