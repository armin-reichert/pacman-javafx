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
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.GameImpl;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.tinylog.Logger;

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

    private static final float ASPECT_RATIO    = 1.6f; // 16:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private static String including(boolean b) {
        return b ? "including" : "not including";
    }

    private static String using(boolean b) {
        return b ? "using" : "without using";
    }

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    private boolean useBuilder;
    private boolean includeTests;

    @Override
    public void init() {
        machine.loadCartridge(ArcadePacMan_Cartridge.CARTRIDGE);
        machine.loadCartridge(ArcadeMsPacMan_Cartridge.CARTRIDGE);
        machine.loadCartridge(TengenMsPacMan_Cartridge.CARTRIDGE);
        machine.loadCartridge(PacManXXL_PacMan_Cartridge.CARTRIDGE);
        machine.loadCartridge(PacManXXL_MsPacMan_Cartridge.CARTRIDGE);

        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final PacManXXL_MapSelector sharedMapSelector = new PacManXXL_MapSelector();

        try {
            if (useBuilder) {
                game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
                    .worldMapSelector(ARCADE_PACMAN_XXL, sharedMapSelector)
                    .worldMapSelector(ARCADE_MS_PACMAN_XXL, sharedMapSelector)
                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)
                    .build(GameUI.DEFAULT_SETTINGS, stage);
            }
            else {
                game = new GameImpl(machine);
                game.createUI(GameUI.DEFAULT_SETTINGS, stage, sceneSize.x(), sceneSize.y());

                game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(sharedMapSelector);
                game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(sharedMapSelector);

                final StartPagesView startPagesView = game.ui().views().assertView(GameViewID.START_PAGES, StartPagesView.class);
                startPagesView.addStartPage(game, new ArcadePacMan_StartPage());
                startPagesView.addStartPage(game, new ArcadeMsPacMan_StartPage());
                startPagesView.addStartPage(game, new TengenMsPacMan_StartPage());
                startPagesView.addStartPage(game, new PacManXXL_StartPage());
                startPagesView.rootPane().setSelectedIndex(0);
            }

            game.extensions().add(Arcade_GameExtensions.ACTIONS, new Arcade_Actions(game));

            game.extensions().add(TengenMsPacMan_GameExtension.ACTIONS, new TengenMsPacMan_Actions(game));
            game.extensions().add(TengenMsPacMan_GameExtension.UI_SETTINGS, new TengenMsPacMan_UISettings());

            //TODO find more elegant solution
            game.watchdog().addEventListener(sharedMapSelector);

            Logger.info("UI created {} builder {} tests", using(useBuilder), including(includeTests));

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