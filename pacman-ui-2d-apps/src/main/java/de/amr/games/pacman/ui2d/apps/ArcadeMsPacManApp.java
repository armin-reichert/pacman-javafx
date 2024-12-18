/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.apps;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacManStartPage;
import de.amr.games.pacman.arcade.ms_pacman.MsPacManGame;
import de.amr.games.pacman.arcade.ms_pacman.MsPacManGameConfiguration;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ArcadeMsPacManApp extends Application {

    @Override
    public void init() {
        try {
            File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
            if (userDir.mkdir()) {
                Logger.info("User dir '{}' created", userDir);
            }
            GameController.it().addGameImplementation(GameVariant.MS_PACMAN, new MsPacManGame(userDir));
            GameController.it().selectGame(GameVariant.MS_PACMAN);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI ui = new PacManGamesUI();
        ui.loadAssets();
        ui.addGameConfiguration(GameVariant.MS_PACMAN, new MsPacManGameConfiguration(ui.assets()));
        ui.create(stage, initialSize());
        ui.addStartPageSlide(new ArcadeMsPacManStartPage().root());

        ui.addDashboardItem(PacManGamesUI.DashboardItemID.README);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.GENERAL);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.GAME_CONTROL);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.GAME_INFO);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.ACTOR_INFO);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.KEYBOARD);
        ui.addDashboardItem(PacManGamesUI.DashboardItemID.ABOUT);
        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = 1.2;
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
