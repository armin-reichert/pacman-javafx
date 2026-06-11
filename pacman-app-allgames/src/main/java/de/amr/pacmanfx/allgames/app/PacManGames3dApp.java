/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.PacManXXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_MapSelector;
import de.amr.pacmanfx.arcade.pacman_xxl.common.PacManXXL_StartPage;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.tengenmspacman.DashboardSectionJoypad;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.GameImpl;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.subviews.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.subviews.dashboard.Dashboard;
import de.amr.pacmanfx.ui.subviews.playview.GamePlayView;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImpl;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.*;

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
public class PacManGames3dApp extends Application {

    private static final float ASPECT_RATIO    = 1.6f; // 16:10
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private static String including(boolean b) {
        return b ? "including" : "not including";
    }

    private static String using(boolean b) {
        return b ? "using" : "without using";
    }

    private static final List<CommonDashboardID> DASHBOARD_IDs = List.of(
        CommonDashboardID.GENERAL,
        CommonDashboardID.GAME_CONTROL,
        CommonDashboardID.SETTINGS_3D,
        CommonDashboardID.ANIMATION_INFO,
        CommonDashboardID.GAME_INFO,
        CommonDashboardID.ACTOR_INFO,
        CommonDashboardID.CUSTOM_MAPS,
        CommonDashboardID.KEYS_GLOBAL,
        CommonDashboardID.KEYS_LOCAL,
        CommonDashboardID.ABOUT
    );

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    private boolean useBuilder;
    private boolean includeTests;

    @Override
    public void init() {
        machine.insertCartridge(ArcadePacMan_Cartridge.CARTRIDGE);
        machine.insertCartridge(ArcadeMsPacMan_Cartridge.CARTRIDGE);
        machine.insertCartridge(TengenMsPacMan_Cartridge.CARTRIDGE);
        machine.insertCartridge(PacManXXL_PacMan_Cartridge.CARTRIDGE);
        machine.insertCartridge(PacManXXL_MsPacMan_Cartridge.CARTRIDGE);

        useBuilder = Boolean.parseBoolean(getParameters().getNamed().get("use_builder"));
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final PacManXXL_MapSelector xxlMapSelector = new PacManXXL_MapSelector();

        try {
            if (useBuilder) {
                game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
                    .worldMapSelector(ARCADE_PACMAN_XXL, xxlMapSelector)
                    .worldMapSelector(ARCADE_MS_PACMAN_XXL, xxlMapSelector)
                    .startPage(ArcadePacMan_StartPage::new)
                    .startPage(ArcadeMsPacMan_StartPage::new)
                    .startPage(TengenMsPacMan_StartPage::new)
                    .startPage(PacManXXL_StartPage::new)
                    .build();
            }
            else {
                game = new GameImpl(machine, new GameViewImpl(sceneSize.x(), sceneSize.y()));
                game.gameVariant(GameVariantID.ARCADE_PACMAN_XXL.name())   .gameModel().setMapSelector(xxlMapSelector);
                game.gameVariant(GameVariantID.ARCADE_MS_PACMAN_XXL.name()).gameModel().setMapSelector(xxlMapSelector);
                addStartPages();
            }

            configureDashboard();
            game.watchdog().addEventListener(xxlMapSelector); //TODO

            Logger.info("UI created {} builder {} tests", using(useBuilder), including(includeTests));
        }
        catch (RuntimeException x) {
            Logger.error(x, "An error occurred starting the game.");
            Platform.exit();
        }

        game.ui().extensions().addExtension(TengenMsPacMan_UIConfig.EXT_UI_SETTINGS, new TengenMsPacMan_UISettings());

        game.show(GameVariantID.ARCADE_PACMAN, stage);
    }

    @Override
    public void stop() {
        if (game != null) {
            game.terminate();
        }
    }

    // Private area

    private void addStartPages() {
        final StartPagesView startView = game.ui().subViews().startView();
        startView.addStartPage(new ArcadePacMan_StartPage());
        startView.addStartPage(new ArcadeMsPacMan_StartPage());
        startView.addStartPage(new TengenMsPacMan_StartPage());
        startView.addStartPage(new PacManXXL_StartPage());
        startView.setSelectedIndex(0);
    }

    //TODO builder support
    private void configureDashboard() {
        final GamePlayView playView = game.ui().subViews().gamePlayView();
        final Dashboard dashboard = playView.dashboard();

        playView.configureDashboard(DASHBOARD_IDs, game.ui().translations());
        // Add "Joypad" section
        dashboard.addSection(
            TengenMsPacMan_DashboardID.JOYPAD,
            new DashboardSectionJoypad(dashboard),
            game.gameVariant(TENGEN_MS_PACMAN.name()).uiConfig().translate("infobox.joypad.title"),
            false);
    }
}