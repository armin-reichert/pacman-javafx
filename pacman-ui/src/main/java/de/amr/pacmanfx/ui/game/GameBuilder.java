/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.game;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.dashboard.CommonDashboardFactory;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.startpages.StartPage;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.uilib.SettingsLoader;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.net.URL;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring a game application.
 */
public class GameBuilder {

    private final Set<Cartridge> cartridgeSet = new HashSet<>();

    private GameUISettings uiSettings;

    private DashboardFactory dashboardFactory;

    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    private Stage stage;
    private int width;
    private int height;

    public GameBuilder() {
        dashboardFactory = CommonDashboardFactory.instance();
        uiSettings = SettingsLoader.load(
            getClass().getResource("/de/amr/pacmanfx/ui/ui.json"),
            GameUISettings.class);
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        height = Math.min(600, (int) bounds.getHeight() * 2 / 3);
        width = height * 28 / 32;
    }

    public GameBuilder cartridges(Cartridge... cartridges) {
        cartridgeSet.addAll(List.of(cartridges));
        return this;
    }

    public GameBuilder size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public GameBuilder screenArea(double aspectRatio, double heightFraction) {
        Vector2i sectionSize = Ufx.computeScreenSectionSize(aspectRatio, heightFraction);
        width = sectionSize.x();
        height = sectionSize.y();
        return this;
    }

    public GameBuilder window(Stage stage) {
        this.stage = requireNonNull(stage);
        return this;
    }

    public GameBuilder dashboardFactory(DashboardFactory dashboardFactory) {
        this.dashboardFactory = requireNonNull(dashboardFactory);
        return this;
    }

    public GameBuilder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public GameBuilder uiSettings(URL url) {
        requireNonNull(url);
        uiSettings = SettingsLoader.load(url, GameUISettings.class);
        return this;
    }

    public Optional<Game> build() {
        try {
            validateConfigurationData();

            var machine = new PacManGamesMachine();
            for (var c : cartridgeSet) {
                machine.loadCartridge(c);
            }

            final var game = new PacManGamesCollection(machine);

            // Add game extensions
            for (var cartridge : cartridgeSet) {
                for (GameExtension extension : cartridge.gameExtensions()) {
                    game.extensions().add(extension);
                }
            }

            game.createUI(
                uiSettings,
                dashboardFactory,
                stage,
                width,
                height);

            final StartPagesView startPagesView = game.ui().viewManager()
                .assertView(GameViewID.START_PAGES, StartPagesView.class);

            for (var factory : startPageFactories) {
                final StartPage page = factory.get();
                if (page != null) {
                    startPagesView.addStartPage(game, page);
                } else {
                    error("Start page could not be created using factory: " + factory);
                }
            }

            return Optional.of(game);
        }
        catch (Exception x) {
            Logger.error("Game building failed: {}", x.getMessage());
            return Optional.empty();
        }
    }

    private void validateConfigurationData() {
        if (cartridgeSet.isEmpty()) {
            error("No cartridges have been inserted into game machine");
        }
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
