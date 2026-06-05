/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameVariant;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.app.GamesContainer;
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
import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring an application.
 */
public class GameAppBuilder {

    record WindowConfig(Stage stage, int sceneWidth, int sceneHeight) {}

    record GameConfig(
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector) {}

    public static GameAppBuilder newApp(
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight,
        GamesContainer gamesContainer,
        CoinMechanism coinMechanism)
    {
        return new GameAppBuilder(stage, mainSceneWidth, mainSceneHeight, gamesContainer, coinMechanism);
    }

    private final WindowConfig windowConfig;
    private final CoinMechanism coinMechanism;
    private final GamesContainer gamesContainer;
    private final Map<String, GameConfig> gameConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();
    private boolean interactiveTests;

    private GameAppBuilder(
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight,
        GamesContainer gamesContainer,
        CoinMechanism coinMechanism)
    {
        windowConfig = new WindowConfig(stage, mainSceneWidth, mainSceneHeight);
        this.gamesContainer = requireNonNull(gamesContainer);
        this.coinMechanism = requireNonNull(coinMechanism);
    }

    /**
     * Example:
     * <pre>
     *     game(
     *      "ARCADE_PACMAN",
     *      () -> new ArcadePacMan_GameModel(gameBox.coinMechanism(),
     *
     * </pre>
     *
     */
    public GameAppBuilder game(
        String gameVariantName,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector)
    {
        validateGameVariantName(gameVariantName);
        if (gameModelFactory == null) {
            error("Game model factory for game variant '%s' is null".formatted(gameVariantName));
        }
        if (uiConfigFactory == null) {
            error("UI configuration factory for game variant '%s' is null".formatted(gameVariantName));
        }
        gameConfigMap.put(gameVariantName, new GameConfig(gameModelFactory, uiConfigFactory, mapSelector));
        return this;
    }

    public GameAppBuilder game(
        String variantName,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory)
    {
        return game(variantName, gameModelFactory, uiConfigFactory, null);
    }

    public GameAppBuilder game(
        GameVariant variant,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory)
    {
        return game(variant.name(), gameModelFactory, uiConfigFactory, null);
    }

    public GameAppBuilder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public GameAppBuilder interactiveTests(boolean include) {
        interactiveTests = include;
        return this;
    }

    private GameViewImpl createViewImplementation(Stage stage, int width, int height) {
        return new GameViewImpl(
            stage,
            new GameViewMainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> AppConstants.LOCALIZED_TEXTS)
        );
    }

    public AppContext build() {
        validateConfigurationData();

        final var ui = new GamesApp(
            gamesContainer,
            createViewImplementation(windowConfig.stage(), windowConfig.sceneWidth(), windowConfig.sceneHeight()),
            new GameClockFX(),
            coinMechanism);

        gameConfigMap.forEach((gameVariant, config) -> {
            final AbstractGameModel game = config.gameModelFactory.get();
            gamesContainer.registerGame(gameVariant, game);
            ui.ui().configurations().addConfigFactory(gameVariant, config.uiConfigFactory);
            if (interactiveTests) {
                final GameFlow gameFlow = game.flow();
                gameFlow.addState(new LevelShortTestState());
                gameFlow.addState(new LevelMediumTestState());
                gameFlow.addState(new CutScenesTestState());
            }
        });

        final StartPagesView startPagesCarousel = ui.ui().subViews().startView();
        for (var startPageFactory : startPageFactories) {
            final StartPage startPage = startPageFactory.get();
            if (startPage != null) {
                startPagesCarousel.addStartPage(startPage);
                startPage.init(ui);
            }
            else {
                error("Start page could not be created");
            }
        }
        return ui;
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
        if (!GamesContainer.GAME_VARIANT_NAME_PATTERN.matcher(name).matches()) {
            error("Game variant name '%s' does not match pattern '%s'".formatted(name, GamesContainer.GAME_VARIANT_NAME_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
