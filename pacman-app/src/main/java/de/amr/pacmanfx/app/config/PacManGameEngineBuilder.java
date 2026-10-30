/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.app.config;

import de.amr.basics.json.JsonLoader;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.rendering.DefaultRenderManager;
import de.amr.basics.ui.spriteanim.SpriteAnimationSystem;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.app.runtime.DefaultGameVariantManager;
import de.amr.pacmanfx.app.runtime.GameEngine;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.gamestate.GameSimulation;
import de.amr.pacmanfx.engine.DefaultGameClock;
import de.amr.pacmanfx.engine.sound.DefaultSoundManager;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.views.DefaultGameViewManager;
import de.amr.pacmanfx.ui.views.dashboard.CommonDashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPage;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring a Pac-Man game "engine".
 */
public class PacManGameEngineBuilder {

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

    private GameUISettings uiSettings;

    private DashboardFactory dashboardFactory;

    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    private Stage stage;
    private int width;
    private int height;

    public PacManGameEngineBuilder() {
        dashboardFactory = CommonDashboardFactory.instance();
        uiSettings = loadDefaultSettings();
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        height = Math.min(600, (int) bounds.getHeight() * 2 / 3);
        width = height * 28 / 32;
    }

    public PacManGameEngineBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PacManGameEngineBuilder screenArea(double aspectRatio, double heightFraction) {
        Vector2i sectionSize = Ufx.computeScreenSectionSize(aspectRatio, heightFraction);
        width = sectionSize.x();
        height = sectionSize.y();
        return this;
    }

    public PacManGameEngineBuilder window(Stage stage) {
        this.stage = requireNonNull(stage);
        return this;
    }

    public PacManGameEngineBuilder dashboardFactory(DashboardFactory dashboardFactory) {
        this.dashboardFactory = requireNonNull(dashboardFactory);
        return this;
    }

    public PacManGameEngineBuilder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public PacManGameEngineBuilder uiSettings(URL url) {
        requireNonNull(url);
        uiSettings = JsonLoader.load(url, GameUISettings.class);
        return this;
    }

    public Optional<GameEngine> buildEngine() {
        try {
            validateConfigurationData();

            final var engine = new GameEngine(
                new DefaultGameClock(GameConstants.SIMULATION_FPS),
                new GameSimulation(),
                new CoinMechanism(99),
                new GameUI(stage, width, height, uiSettings),
                new DefaultGameVariantManager(),
                new DefaultGameSceneManager(),
                new DefaultGameViewManager(),
                new DefaultRenderManager(),
                new DefaultSoundManager(),
                new SpriteAnimationSystem(60)
            );

            // Requires that UI has been assigned to game:

            final StartPagesView startPagesView = engine.viewManager().startPagesView();
            createStartPages().forEach(page -> startPagesView.addPage(engine, page));

            final GamePlayView playView = engine.viewManager().gamePlayView();
            playView.populateDashboard(dashboardFactory);

            return Optional.of(engine);
        }
        catch (Exception x) {
            Logger.error(x, "Game engine building failed");
            return Optional.empty();
        }
    }

    private List<StartPage> createStartPages() {
        final List<StartPage> pages = new ArrayList<>();
        for (var factory : startPageFactories) {
            final StartPage page = factory.get();
            if (page != null) {
                pages.add(page);
            } else {
                error("Start page could not be created using factory: " + factory);
            }
        }
        return pages;
    }

    private void validateConfigurationData() {
        if (stage == null) {
            error("No stage has been specified");
        }
        if (width <= 0) {
            error("Main scene width must be a positive number");
        }
        if (height <= 0) {
            error("Main scene height must be a positive number");
        }
        if (uiSettings == null) {
            error("No UI settings have been specified");
        }
        if (startPageFactories.isEmpty()) {
            error("No start page specified, don't know how to start your game");
        }
    }

    private void error(String message) {
        throw new RuntimeException(message);
    }
}
