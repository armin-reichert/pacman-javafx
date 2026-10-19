/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.allgames.app;

import de.amr.basics.json.JsonLoader;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.rendering.DefaultRenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.app.runtime.DefaultGameVariantManager;
import de.amr.pacmanfx.app.runtime.GameEngine;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_StartPage;
import de.amr.pacmanfx.arcade.ms_pacman.app.ArcadeMsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_StartPage;
import de.amr.pacmanfx.arcade.pacman.app.ArcadePacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.XXL_MsPacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.app.XXL_PacMan_Cartridge;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_StartPage;
import de.amr.pacmanfx.arcade.pacman_xxl.common.XXL_WorldMapManager;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.engine.CartridgeRegistry;
import de.amr.pacmanfx.engine.sound.DefaultSoundManager;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_StartPage;
import de.amr.pacmanfx.tengenmspacman.app.TengenMsPacMan_Cartridge;
import de.amr.pacmanfx.tengenmspacman.dashboard.TengenDashboardFactory;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.views.DefaultGameViewManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.application.Application;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.net.URL;

public class PacManAllGamesNoBuilder extends Application {

    static final String DEFAULT_UI_SETTINGS_PATH = "/de/amr/pacmanfx/ui/ui.json";

    private static GameUISettings loadDefaultSettings() {
        final URL url = GameUI.class.getResource(DEFAULT_UI_SETTINGS_PATH);
        if (url == null) {
            throw new IllegalArgumentException("Could not load default UI settings file from path '%s'"
                .formatted(DEFAULT_UI_SETTINGS_PATH));
        }
        final var settings = JsonLoader.load(url, GameUISettings.class);
        Logger.info("Default UI settings loaded, URL={}", url);
        return settings;
    }

    static final float ASPECT_RATIO    = 1.6f; // 16:10
    static final float HEIGHT_FRACTION = 0.8f; // Use 80% of screen height

    private GameEngine engine;

    private boolean includeTests;

    @Override
    public void init() {
        includeTests = Boolean.parseBoolean(getParameters().getNamed().get("include_tests"));
        CartridgeRegistry.instance().register(
            ArcadePacMan_Cartridge.CARTRIDGE,
            ArcadeMsPacMan_Cartridge.CARTRIDGE,
            TengenMsPacMan_Cartridge.CARTRIDGE,
            XXL_PacMan_Cartridge.CARTRIDGE,
            XXL_MsPacMan_Cartridge.CARTRIDGE
        );
        engine = new GameEngine(
            new DefaultGameVariantManager(),
            new DefaultGameSceneManager(),
            new DefaultGameViewManager(),
            new DefaultRenderManager(),
            new DefaultSoundManager(),
            new SpriteAnimationSystem(60)
        );
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);
        final GameUI ui = new GameUI(stage, sceneSize.x(), sceneSize.y(), loadDefaultSettings());

        final StartPagesView startPages = engine.viewManager().assertView(GameViewID.START_PAGES, StartPagesView.class);
        startPages.addPage(engine, new ArcadePacMan_StartPage());
        startPages.addPage(engine, new ArcadeMsPacMan_StartPage());
        startPages.addPage(engine, new TengenMsPacMan_StartPage());
        startPages.addPage(engine, new XXL_StartPage("Pac-Man XXL games")); //TODO localize

        engine.watchdog().addEventListener(XXL_WorldMapManager.instance());
        engine.setUI(ui);

        final GamePlayView playView = engine.viewManager().assertView(GameViewID.GAMEPLAY, GamePlayView.class);
        playView.populateDashboard(TengenDashboardFactory.instance());

        engine.showGameVariant(GameVariantID.ARCADE_PACMAN);

        // This must happen *after* UI has been set!
        startPages.rootPane().setSelectedIndex(0);
    }

    @Override
    public void stop() {
        if (engine != null) {
            engine.terminate();
        }
    }
}