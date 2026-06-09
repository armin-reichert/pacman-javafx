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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static de.amr.pacmanfx.core.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring a game application.
 */
public class GameBuilder {

    record WindowConfig(int sceneWidth, int sceneHeight) {}

    record GameVariantConfig(
        WorldMapSelector mapSelector,
        boolean includeTests) {}

    public static GameBuilder compose(
        PacManGamesMachine gamesCollection,
        int mainSceneWidth,
        int mainSceneHeight)
    {
        return new GameBuilder(gamesCollection, mainSceneWidth, mainSceneHeight);
    }

    private final WindowConfig windowConfig;
    private final Map<String, GameVariantConfig> gameVariantConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    private PacManGamesMachine machine;
    private boolean coinMechanism;

    private GameBuilder(
        PacManGamesMachine machine,
        int mainSceneWidth,
        int mainSceneHeight)
    {
        this.machine = requireNonNull(machine);
        windowConfig = new WindowConfig(mainSceneWidth, mainSceneHeight);
    }

    public GameBuilder machine(PacManGamesMachine machine) {
        this.machine = machine;
        return this;
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

        final var game = new GameImplementation(
            machine,
            createGameView(windowConfig.sceneWidth(), windowConfig.sceneHeight()),
            new GameClockFX(),
            coinMechanism ? new CoinMechanism(99) : CoinMechanism.OUT_OF_SERVICE);

        final StartPagesView startPagesCarousel = game.ui().subViews().startView();
        for (var startPageFactory : startPageFactories) {
            final StartPage startPage = startPageFactory.get();
            if (startPage != null) {
                startPagesCarousel.addStartPage(startPage);
                startPage.init(game);
            }
            else {
                error("Start page could not be created");
            }
        }
        return game;
    }

    private GameViewImplementation createGameView(int width, int height) {
        return new GameViewImplementation(
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
