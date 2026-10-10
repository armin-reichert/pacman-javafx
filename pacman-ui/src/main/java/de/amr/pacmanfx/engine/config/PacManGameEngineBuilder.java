/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.engine.config;

import de.amr.basics.json.JsonLoader;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.engine.runtime.PacManGamesEngine;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.dashboard.CommonDashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
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

    private GameUISettings uiSettings;

    private DashboardFactory dashboardFactory;

    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    private Stage stage;
    private int width;
    private int height;

    public PacManGameEngineBuilder() {
        dashboardFactory = CommonDashboardFactory.instance();
        uiSettings = JsonLoader.load(
            getClass().getResource("/de/amr/pacmanfx/ui/ui.json"),
            GameUISettings.class);
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

    public Optional<PacManGamesEngine> buildEngine() {
        try {
            validateConfigurationData();

            final var engine = new PacManGamesEngine();

            final GameUI ui = new GameUI(stage, width, height, uiSettings);
            engine.setUI(ui, dashboardFactory);

            // Can only be done after UI has been assigned to game!
            final StartPagesView startPagesView = ui.viewManager().reqView(GameViewID.START_PAGES, StartPagesView.class);
            addStartPages(engine, startPagesView);

            return Optional.of(engine);
        }
        catch (Exception x) {
            Logger.error(x, "Game building failed");
            return Optional.empty();
        }
    }

    private void addStartPages(GameEngineContext actionContext, StartPagesView startPagesView) {
        for (var factory : startPageFactories) {
            final StartPage page = factory.get();
            if (page != null) {
                startPagesView.addStartPage(actionContext, page);
            } else {
                error("Start page could not be created using factory: " + factory);
            }
        }
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
