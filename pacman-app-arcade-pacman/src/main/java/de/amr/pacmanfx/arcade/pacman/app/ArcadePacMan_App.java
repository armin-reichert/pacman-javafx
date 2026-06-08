/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.ui.game.*;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImplementation;
import de.amr.pacmanfx.ui.view.GameViewMainScene;
import de.amr.pacmanfx.ui.view.StatusIconBox;
import de.amr.pacmanfx.uilib.GameClockFX;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static de.amr.pacmanfx.core.Validations.requireNonNegative;
import static de.amr.pacmanfx.uilib.Ufx.computeScreenSectionSize;

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

    private GamesCollection gamesCollection;
    private Game app;
    private boolean useBuilder;

    @Override
    public void init() throws Exception {
        gamesCollection = new GamesCollection();
        gamesCollection.registerGame(GameVariant.ARCADE_PACMAN.name(), new GameVariantSpecification(
            Arcade_GameFlow::new,
            ArcadePacMan_GameModel::new,
            ArcadePacMan_GameRules::new,
            false
        ));

        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i size = computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        if (useBuilder) {
            app = GameBuilder.compose(gamesCollection, primaryStage, size.x(), size.y())
                .gameVariant(
                    GameVariant.ARCADE_PACMAN,
                    Arcade_GameFlow::new,
                    ArcadePacMan_GameModel::new,
                    ArcadePacMan_GameRules::new,
                    ArcadePacMan_UIConfig::new)
                .startPage(ArcadePacMan_StartPage::new)
                .coinMechanism(true)
                .interactiveTests(false)
                .build();
        }
        else {
            createApp(primaryStage, size);
        }
        app.ui().subViews().gamePlayView().configureDashboard(DASHBOARD_IDs, app.ui().translations());
        app.displayOnScreen();
    }

    @Override
    public void stop() {
        app.terminate();
    }

    // Private area

    private void createApp(Stage stage, Vector2i sceneSize) {
        app = new GameImplementation(
            gamesCollection,
            createView(stage, sceneSize.x(), sceneSize.y()),
            new GameClockFX(),
            new CoinMechanism());

        app.ui().configurations().addConfigFactory(
            GameVariant.ARCADE_PACMAN.name(), ArcadePacMan_UIConfig::new);

        final StartPagesView startView = app.ui().subViews().startView();

        final var arcadePacManStartPage = new ArcadePacMan_StartPage();
        arcadePacManStartPage.init(app);

        startView.addStartPage(arcadePacManStartPage);
        startView.setSelectedIndex(0);
    }

    private GameViewImplementation createView(Stage stage, int width, int height) {
        return new GameViewImplementation(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> GameConstants.LOCALIZED_TEXTS)
        );
    }
}