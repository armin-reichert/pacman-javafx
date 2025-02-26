/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameConfig3D;
import de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel;
import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.DashboardItemID;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui3d.dashboard.InfoBox3D;
import javafx.application.Application;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;

public class ArcadePacManXXL_App extends Application {

    @Override
    public void init() {
        try {
            File userDir = new File(System.getProperty("user.home"), ".pacmanfx");
            if (userDir.mkdir()) {
                Logger.info("User dir '{}' created", userDir);
            }
            GameController.it().addGame(GameVariant.PACMAN_XXL, new ArcadePacManXXL_GameModel(userDir));
            GameController.it().addGame(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameModel(userDir));
            GameController.it().selectGame(GameVariant.PACMAN_XXL);
        } catch (Exception x) {
            x.printStackTrace(System.err);
        }
    }

    @Override
    public void start(Stage stage) {
        PacManGamesUI_3D ui = new PacManGamesUI_3D();
        ui.loadAssets();

        ui.configureGameVariant(GameVariant.MS_PACMAN, new ArcadeMsPacMan_GameConfig3D(ui.assets()));
        ui.configureGameVariant(GameVariant.PACMAN_XXL, new ArcadePacManXXL_GameConfig3D(ui.assets()));

        ui.create(stage, initialSize());

        PacManXXLOptionsMenu optionsMenu = new PacManXXLOptionsMenu(ui);
        ui.setStartPage(GameVariant.PACMAN_XXL, optionsMenu);

        ui.addDashboardItem(DashboardItemID.README);
        ui.addDashboardItem(DashboardItemID.GENERAL);
        ui.addDashboardItem(DashboardItemID.GAME_CONTROL);
        ui.addDashboardItem(ui.locText("infobox.3D_settings.title"), new InfoBox3D());
        ui.addDashboardItem(DashboardItemID.GAME_INFO);
        ui.addDashboardItem(DashboardItemID.ACTOR_INFO);
        ui.addDashboardItem(ui.locText("infobox.custom_maps.title"), new InfoBoxCustomMaps());
        ui.addDashboardItem(DashboardItemID.KEYBOARD);
        ui.addDashboardItem(DashboardItemID.ABOUT);
        ui.show();
    }

    private static Dimension2D initialSize() {
        Rectangle2D screenSize = Screen.getPrimary().getBounds();
        double aspect = screenSize.getWidth() / screenSize.getHeight();
        double height = 0.8 * screenSize.getHeight();
        return new Dimension2D(aspect * height, height);
    }
}
