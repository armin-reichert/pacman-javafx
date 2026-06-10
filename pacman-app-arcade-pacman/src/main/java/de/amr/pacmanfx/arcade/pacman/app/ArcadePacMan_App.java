/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GlobalsUI;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.GameImplementation;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.view.GameViewImplementation;
import de.amr.pacmanfx.ui.view.GameViewMainScene;
import de.amr.pacmanfx.ui.view.StatusIconBox;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static de.amr.pacmanfx.uilib.Ufx.computeScreenSectionSize;

public class ArcadePacMan_App extends Application {

    private static final float ASPECT_RATIO    = 1.2f; // 12:10 aspect ratio
    private static final float HEIGHT_FRACTION = 0.8f; // 80% of available height

    private static final List<CommonDashboardID> DASHBOARD_IDs = List.of(
        CommonDashboardID.GENERAL, CommonDashboardID.GAME_CONTROL, CommonDashboardID.SETTINGS_3D,
        CommonDashboardID.ANIMATION_INFO, CommonDashboardID.GAME_INFO, CommonDashboardID.ACTOR_INFO,
        CommonDashboardID.KEYS_GLOBAL, CommonDashboardID.KEYS_LOCAL, CommonDashboardID.ABOUT);

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;
    private boolean useBuilder;

    @Override
    public void init() {
        machine.insertCartridge(GameVariantID.ARCADE_PACMAN.name(), ArcadePacMan_Cartridge.CARTRIDGE);
        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
    }

    @Override
    public void start(Stage primaryStage) {
        final Vector2i size = computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        if (useBuilder) {
            game = GameBuilder.compose(machine, size.x(), size.y())
                .gameVariant(GameVariantID.ARCADE_PACMAN.name())
                .startPage(ArcadePacMan_StartPage::new)
                .build();
        }
        else {
            game = new GameImplementation(machine,
                new GameViewImplementation(
                    new GameViewMainScene(size.x(), size.y()),
                    new StatusIconBox(() -> GlobalsUI.LOCALIZED_TEXTS)));
            final var arcadePacManStartPage = new ArcadePacMan_StartPage();
            arcadePacManStartPage.init(game);
            game.ui().subViews().startView().addStartPage(arcadePacManStartPage);
            game.ui().subViews().startView().setSelectedIndex(0);
        }
        game.ui().subViews().gamePlayView().configureDashboard(DASHBOARD_IDs, game.ui().translations());
        game.selectGameVariant(GameVariantID.ARCADE_PACMAN.name());
        game.show(primaryStage);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}