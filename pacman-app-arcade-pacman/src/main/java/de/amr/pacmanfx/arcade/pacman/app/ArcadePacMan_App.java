/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Builder;
import de.amr.pacmanfx.ui.GameUI_Implementation;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.layout.StartPagesCarousel;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.uilib.GameClockFX;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

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
    private GameUI ui;

    @Override
    public void init() throws Exception {
        gameBox = new GameBox(new GameClockFX(), new CoinMechanism(99));
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i size = computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        // command-line argument: --use_builder=true
        final String argVal = getParameters().getNamed().get("use_builder");
        final boolean useBuilder = Boolean.parseBoolean(argVal);

        if (useBuilder) {
            ui = GameUI_Builder.newUI(primaryStage, size.x(), size.y(), gameBox)
                .game(
                    GameVariant.ARCADE_PACMAN,
                    () -> new ArcadePacMan_GameModel(gameBox.coinMechanism()),
                    ArcadePacMan_UIConfig::new)
                .startPage(ArcadePacMan_StartPage::new)
                .build();
        }
        else {
            createUI(primaryStage, gameBox, size);
        }
        ui.services().configureDashboard(DASHBOARD_IDs);
        ui.show();
    }

    @Override
    public void stop() {
        ui.terminate();
    }

    // Private area

    private void createUI(Stage stage, GameBox gameBox, Vector2i size) {
        final var game = new ArcadePacMan_GameModel(gameBox.coinMechanism());

        gameBox.registerGame(GameVariant.ARCADE_PACMAN.name(), game);

        ui = new GameUI_Implementation(gameBox, stage, size.x(), size.y());
        ui.services().configurations().addConfigFactory(
            GameVariant.ARCADE_PACMAN.name(), ArcadePacMan_UIConfig::new);

        final StartPagesCarousel startView = ui.services().views().startView();

        final var arcadePacManStartPage = new ArcadePacMan_StartPage();
        arcadePacManStartPage.init(ui);

        startView.addStartPage(arcadePacManStartPage);
        startView.setSelectedIndex(0);
    }
}