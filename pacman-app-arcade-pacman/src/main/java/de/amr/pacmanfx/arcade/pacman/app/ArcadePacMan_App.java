/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

public class ArcadePacMan_App extends Application {

    private static final String NAME_OF_THE_GAME = GameVariant.ARCADE_PACMAN.name();

    private static final float ASPECT_RATIO    = 1.2f; // 12:10 aspect ratio
    private static final float HEIGHT_FRACTION = 0.8f;  // 80% of available height

    private static final List<CommonDashboardID> DASHBOARD_IDs = List.of(
        CommonDashboardID.GENERAL,
        CommonDashboardID.GAME_CONTROL,
        CommonDashboardID.SETTINGS_3D,
        CommonDashboardID.ANIMATION_INFO,
        CommonDashboardID.GAME_INFO,
        CommonDashboardID.ACTOR_INFO,
        CommonDashboardID.KEYS_GLOBAL,
        CommonDashboardID.KEYS_LOCAL,
        CommonDashboardID.ABOUT
    );

    private GameUI ui;

    private Dimension2D sceneSize() {
        final double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();
        final double height = Math.floor(HEIGHT_FRACTION * screenHeight);
        final double width  = Math.floor(ASPECT_RATIO * height);
        return new Dimension2D(width, height);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            final boolean useBuilder = Boolean.parseBoolean(getParameters().getNamed().getOrDefault("use_builder", "true"));
            if (useBuilder) createUI_WithBuilder(primaryStage, sceneSize());
            else            createUI(primaryStage, sceneSize());
            ui.show();
        }
        catch (RuntimeException x) {
            Logger.error("An error occurred while UI creation.");
            Logger.error(x);
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        ui.terminate();
    }

    private void createUI(Stage stage, Dimension2D size) {
        final File highScoreFile = GameBox.highScoreFile(NAME_OF_THE_GAME);
        final Game game = new ArcadePacMan_GameModel(THE_GAME_BOX, highScoreFile);
        THE_GAME_BOX.registerGame(NAME_OF_THE_GAME, game);

        ui = new GameUI_Implementation(
            Map.of(NAME_OF_THE_GAME, ArcadePacMan_UIConfig.class),
            THE_GAME_BOX,
            stage,
            size.getWidth(), size.getHeight());

        final StartPagesCarousel startPagesView = ui.views().startPagesView();
        final ArcadePacMan_StartPage startPage = new ArcadePacMan_StartPage();
        startPagesView.addStartPage(startPage);
        startPage.init(ui);
        startPagesView.setSelectedIndex(0);

        ui.views().playView().dashboard().addCommonSections(ui, DASHBOARD_IDs);
    }

    private void createUI_WithBuilder(Stage stage, Dimension2D size) {
        ui = GameUI_Builder
            .create(stage, size.getWidth(), size.getHeight())
            .game(NAME_OF_THE_GAME, ArcadePacMan_GameModel.class, ArcadePacMan_UIConfig.class)
            .startPage(ArcadePacMan_StartPage.class, NAME_OF_THE_GAME)
            .dashboard(DASHBOARD_IDs.toArray(CommonDashboardID[]::new))
            .build();
    }
}