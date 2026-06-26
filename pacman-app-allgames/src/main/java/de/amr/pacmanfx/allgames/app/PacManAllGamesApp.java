/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.tengenmspacman.*;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.game.*;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.ARCADE_MS_PACMAN_XXL;
import static de.amr.pacmanfx.core.GameVariantID.ARCADE_PACMAN_XXL;

/**
 * Application containing all game variants, the 3D play scenes, the map editor etc.
 * ("All you can f** ähm play").
 * <p>
 * Command-line arguments:
 * <ul>
 *     <li>{@code --use_builder=value}:<br/>
 *     <li>{@code --include_tests=value}</li>
 * </ul>
 * </p>
 */
public class PacManAllGamesApp extends Application {

    static final float ASPECT_RATIO    = 1.6f; // 16:10
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private static final Cartridge[] CARTRIDGES = {
        ArcadePacMan_Cartridge.CARTRIDGE,
        ArcadeMsPacMan_Cartridge.CARTRIDGE,
        TengenMsPacMan_Cartridge.CARTRIDGE,
        PacManXXL_PacMan_Cartridge.CARTRIDGE,
        PacManXXL_MsPacMan_Cartridge.CARTRIDGE
    };

    Game game;

    // Only for testing API vs builder
    boolean useBuilder;
    boolean includeTests;

    @Override
    public void init() {
        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        PacManXXL_MapSelector sharedMapSelector = new PacManXXL_MapSelector();
        try {
            if (useBuilder) {
                new GameBuilder()
                    .cartridges(CARTRIDGES)
                    .dashboardFactory(TengenDashboardFactory.instance())
                    .worldMapSelector(ARCADE_PACMAN_XXL, sharedMapSelector)
                    .worldMapSelector(ARCADE_MS_PACMAN_XXL, sharedMapSelector)
                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)
                    .window(stage)
                    .screenArea(ASPECT_RATIO, HEIGHT_FRACTION)
                    .build()
                    .ifPresent(game -> {
                        this.game = game;

                        game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));

                        game.extensions().add(TengenMsPacMan_GameExtension.ACTIONS,
                            new TengenMsPacMan_Actions(game));
                        game.extensions().add(TengenMsPacMan_GameExtension.UI_SETTINGS,
                            new TengenMsPacMan_UISettings());

                        //TODO find more elegant solution
                        game.watchdog().addEventListener(sharedMapSelector);
                        game.showUI(GameVariantID.ARCADE_PACMAN);
                    });
            }
            else {
                var machine = new PacManGamesMachine(List.of(CARTRIDGES));
                Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
                game = new GameImpl(machine);
                game.createUI(
                    GameUI.DEFAULT_SETTINGS,
                    TengenDashboardFactory.instance(),
                    stage, sceneSize.x(), sceneSize.y());

                game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())
                    .gameModel().setMapSelector(sharedMapSelector);
                game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name())
                    .gameModel().setMapSelector(sharedMapSelector);

                StartPagesView startPagesView = game.ui().views().assertView(
                    GameViewID.START_PAGES, StartPagesView.class);

                startPagesView.addStartPage(game, new ArcadePacMan_StartPage());
                startPagesView.addStartPage(game, new ArcadeMsPacMan_StartPage());
                startPagesView.addStartPage(game, new TengenMsPacMan_StartPage());
                startPagesView.addStartPage(game, new PacManXXL_StartPage());

                startPagesView.rootPane().setSelectedIndex(0);
            }

            game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));

            game.extensions().add(TengenMsPacMan_GameExtension.ACTIONS,
                new TengenMsPacMan_Actions(game));
            game.extensions().add(TengenMsPacMan_GameExtension.UI_SETTINGS,
                new TengenMsPacMan_UISettings());

            //TODO find more elegant solution
            game.watchdog().addEventListener(sharedMapSelector);
            game.showUI(GameVariantID.ARCADE_PACMAN);
        }
        catch (RuntimeException x) {
            Logger.error(x, "An error occurred starting the game.");
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        game.terminate();
    }
}