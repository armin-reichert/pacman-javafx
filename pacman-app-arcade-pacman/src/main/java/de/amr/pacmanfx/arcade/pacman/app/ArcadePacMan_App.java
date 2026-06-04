/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameFlow;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.AppContextImpl;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImpl;
import de.amr.pacmanfx.ui.view.GameViewMainScene;
import de.amr.pacmanfx.ui.view.StatusIconBox;
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

    private GameBox gameBox;
    private AppContext context;
    private boolean useBuilder;

    @Override
    public void init() throws Exception {
        gameBox = new GameBox( new CoinMechanism(99));
        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i size = computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        if (useBuilder) {
            context = GameUI_Builder.newUI(primaryStage, size.x(), size.y(), gameBox)
                .game(
                    GameVariant.ARCADE_PACMAN,
                    () -> new ArcadePacMan_GameModel(new Arcade_GameFlow(gameBox), gameBox.coinMechanism()),
                    ArcadePacMan_UIConfig::new)
                .startPage(ArcadePacMan_StartPage::new)
                .build();
        }
        else {
            createUI(primaryStage, gameBox, size);
        }
        context.ui().subViews().gamePlayView().configureDashboard(DASHBOARD_IDs, context.ui().translations());
        context.displayOnScreen();
    }

    @Override
    public void stop() {
        context.terminate();
    }

    // Private area

    private void createUI(Stage stage, GameBox gameBox, Vector2i sceneSize) {
        final var game = new ArcadePacMan_GameModel(new Arcade_GameFlow(gameBox), gameBox.coinMechanism());

        gameBox.registerGame(GameVariant.ARCADE_PACMAN.name(), game);

        context = new AppContextImpl(gameBox,
            createViewImplementation(stage, sceneSize.x(), sceneSize.y())
        );
        context.ui().configurations().addConfigFactory(
            GameVariant.ARCADE_PACMAN.name(), ArcadePacMan_UIConfig::new);

        final StartPagesView startView = context.ui().subViews().startView();

        final var arcadePacManStartPage = new ArcadePacMan_StartPage();
        arcadePacManStartPage.init(context);

        startView.addStartPage(arcadePacManStartPage);
        startView.setSelectedIndex(0);
    }

    private GameViewImpl createViewImplementation(Stage stage, int width, int height) {
        return new GameViewImpl(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> AppConstants.LOCALIZED_TEXTS)
        );
    }

}