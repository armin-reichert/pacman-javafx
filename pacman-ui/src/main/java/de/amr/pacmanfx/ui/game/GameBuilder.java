/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.subviews.startpages.StartPage;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImplementation;
import de.amr.pacmanfx.ui.view.GameViewMainScene;
import de.amr.pacmanfx.ui.view.StatusIconBox;
import de.amr.pacmanfx.uilib.GameClockFX;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static de.amr.pacmanfx.core.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring an application.
 */
public class GameBuilder {

    record WindowConfig(Stage stage, int sceneWidth, int sceneHeight) {}

    record GameVariantConfig(
        WorldMapSelector mapSelector,
        boolean includeTests) {}

    public static GameBuilder compose(
        GamesCollection gamesCollection,
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight)
    {
        return new GameBuilder(gamesCollection, stage, mainSceneWidth, mainSceneHeight);
    }

    private final GamesCollection gamesCollection;
    private final WindowConfig windowConfig;
    private final Map<String, GameVariantConfig> gameVariantConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    private boolean coinMechanism;

    private GameBuilder(
        GamesCollection gamesCollection,
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight)
    {
        this.gamesCollection = requireNonNull(gamesCollection);
        windowConfig = new WindowConfig(stage, mainSceneWidth, mainSceneHeight);
    }

    public GameBuilder coinMechanism(boolean coinMechanism) {
        this.coinMechanism = coinMechanism;
        return this;
    }

    public GameBuilder gameVariant(
        String gameVariantName,
        WorldMapSelector mapSelector,
        boolean includeTests)
    {
        validateGameVariantName(gameVariantName);
        gameVariantConfigMap.put(gameVariantName, new GameVariantConfig(mapSelector, includeTests));
        return this;
    }

    public GameBuilder gameVariant(
        String gameVariantName,
        boolean includeTests)
    {
        validateGameVariantName(gameVariantName);
        gameVariantConfigMap.put(gameVariantName, new GameVariantConfig(null, includeTests));
        return this;
    }

    public GameBuilder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public Game build() {
        validateConfigurationData();

        final var app = new GameImplementation(
            gamesCollection,
            createGameView(windowConfig.stage(), windowConfig.sceneWidth(), windowConfig.sceneHeight()),
            new GameClockFX(),
            coinMechanism ? new CoinMechanism(99) : CoinMechanism.OUT_OF_SERVICE);

        final StartPagesView startPagesCarousel = app.ui().subViews().startView();
        for (var startPageFactory : startPageFactories) {
            final StartPage startPage = startPageFactory.get();
            if (startPage != null) {
                startPagesCarousel.addStartPage(startPage);
                startPage.init(app);
            }
            else {
                error("Start page could not be created");
            }
        }
        return app;
    }

    private GameViewImplementation createGameView(Stage stage, int width, int height) {
        return new GameViewImplementation(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> GameConstants.LOCALIZED_TEXTS)
        );
    }

    private void validateConfigurationData() {
        if (gameVariantConfigMap.isEmpty()) {
            error("No game configuration specified");
        }
        if (windowConfig.sceneWidth() <= 0) {
            error("Main scene width must be a positive number");
        }
        if (windowConfig.sceneHeight() <= 0) {
            error("Main scene height must be a positive number");
        }
    }

    private void validateGameVariantName(String name) {
        if (name == null) {
            error("Game variant name must not be null");
        }
        if (name.isBlank()) {
            error("Game variant name must not be blank");
        }
        if (!GameConstants.GAME_VARIANT_NAME_PATTERN.matcher(name).matches()) {
            error("Game variant name '%s' does not match pattern '%s'".formatted(name, GameConstants.GAME_VARIANT_NAME_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
