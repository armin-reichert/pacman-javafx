/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.app;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.subviews.startpages.StartPage;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.view.GameViewImpl;
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

/**
 * Builder for constructing and configuring an application.
 */
public class AppBuilder {

    record WindowConfig(Stage stage, int sceneWidth, int sceneHeight) {}

    record GameConfig(
        Supplier<? extends GameFlow> gameFlowFactory,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends GameRules> gameRulesFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector) {}

    public static AppBuilder newApp(
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight)
    {
        return new AppBuilder(stage, mainSceneWidth, mainSceneHeight);
    }

    private final WindowConfig windowConfig;
    private final Map<String, GameConfig> gameConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    private boolean coinMechanism;
    private boolean includeTests;

    private AppBuilder(
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight)
    {
        windowConfig = new WindowConfig(stage, mainSceneWidth, mainSceneHeight);
    }

    public AppBuilder coinMechanism(boolean coinMechanism) {
        this.coinMechanism = coinMechanism;
        return this;
    }

    public AppBuilder game(
        String gameVariantName,
        Supplier<? extends GameFlow> gameFlowFactory,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends GameRules> gameRulesFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector)
    {
        validateGameVariantName(gameVariantName);

        if (gameFlowFactory == null) {
            error("Game flow factory for game variant '%s' is null".formatted(gameVariantName));
        }
        if (gameModelFactory == null) {
            error("Game model factory for game variant '%s' is null".formatted(gameVariantName));
        }
        if (gameRulesFactory == null) {
            error("Game rules factory for game variant '%s' is null".formatted(gameVariantName));
        }
        if (uiConfigFactory == null) {
            error("UI configuration factory for game variant '%s' is null".formatted(gameVariantName));
        }
        gameConfigMap.put(gameVariantName, new GameConfig(gameFlowFactory, gameModelFactory, gameRulesFactory, uiConfigFactory, mapSelector));
        return this;
    }

    public AppBuilder game(
        String variantName,
        Supplier<? extends GameFlow> gameFlowFactory,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends GameRules> gameRulesFactory,
        Supplier<? extends UIConfig> uiConfigFactory)
    {
        return game(variantName, gameFlowFactory, gameModelFactory, gameRulesFactory, uiConfigFactory, null);
    }

    public AppBuilder game(
        GameVariant variant,
        Supplier<? extends GameFlow> gameFlowFactory,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends GameRules> gameRulesFactory,
        Supplier<? extends UIConfig> uiConfigFactory)
    {
        return game(variant.name(), gameFlowFactory, gameModelFactory, gameRulesFactory, uiConfigFactory, null);
    }

    public AppBuilder interactiveTests(boolean include) {
        includeTests = include;
        return this;
    }

    public AppBuilder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public Game build() {
        validateConfigurationData();

        final var app = new AppContextImpl(
            createGameView(windowConfig.stage(), windowConfig.sceneWidth(), windowConfig.sceneHeight()),
            new GameClockFX(),
            coinMechanism ? new CoinMechanism(99) : CoinMechanism.OUT_OF_SERVICE);

        gameConfigMap.forEach((variant, game) -> {
            app.gamesContainer().registerGame(variant,
                new GameVariantSpecification(
                    game.gameFlowFactory,
                    game.gameModelFactory.get(),
                    game.gameRulesFactory.get(),
                    includeTests));
            app.ui().configurations().addConfigFactory(variant, game.uiConfigFactory);
        });

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

    private GameViewImpl createGameView(Stage stage, int width, int height) {
        return new GameViewImpl(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> AppConstants.LOCALIZED_TEXTS)
        );
    }

    private void validateConfigurationData() {
        if (gameConfigMap.isEmpty()) {
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
        if (!AppConstants.GAME_VARIANT_NAME_PATTERN.matcher(name).matches()) {
            error("Game variant name '%s' does not match pattern '%s'".formatted(name, AppConstants.GAME_VARIANT_NAME_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
