/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameBox;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.ui.layout.ViewManager;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.List;

public class ArcadePacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10 aspect ratio
    private static final float HEIGHT_FRACTION = 0.8f; // 80% of available height

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

    @Override
    public void start(Stage primaryStage) {
        final var gameBox = new GameBox();
        final File highScoreFile = gameBox.highScoreFile(GameVariant.ARCADE_PACMAN);
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        try {
            final boolean useBuilder = Boolean.parseBoolean(getParameters().getNamed().getOrDefault("use_builder", "true"));
            if (useBuilder) {
                ui = GameUI_Builder
                    .newUI(primaryStage, sceneSize.x(), sceneSize.y(), gameBox)
                    .game(GameVariant.ARCADE_PACMAN,
                        () -> new ArcadePacMan_GameModel(gameBox, highScoreFile), ArcadePacMan_UIConfig::new)
                    .startPage(ArcadePacMan_StartPage::new)
                    .dashboard(DASHBOARD_IDs.toArray(CommonDashboardID[]::new))
                    .build();
            }
            else createUI(primaryStage, gameBox, highScoreFile, sceneSize);
            ui.show();
        }
        catch (RuntimeException x) {
            Logger.error(x, "An error occurred while UI creation.");
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        ui.terminate();
    }

    private void createUI(Stage stage, GameBox gameBox, File highScoreFile, Vector2i size) {
        final Game game = new ArcadePacMan_GameModel(gameBox, highScoreFile);
        gameBox.registerGame(GameVariant.ARCADE_PACMAN.name(), game);

        ui = new GameUI_Implementation(gameBox, stage, size.x(), size.y());
        ui.uiConfigManager().addFactory(GameVariant.ARCADE_PACMAN.name(), ArcadePacMan_UIConfig::new);

        final ArcadePacMan_StartPage startPage = new ArcadePacMan_StartPage();
        startPage.init(ui);

        final StartPagesCarousel startPagesCarousel = ui.views().getView(ViewManager.ViewID.START_VIEW, StartPagesCarousel.class);
        startPagesCarousel.addStartPage(startPage);
        startPagesCarousel.setSelectedIndex(0);

        ui.dashboard().addCommonSections(ui, DASHBOARD_IDs);
    }
}