/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.GameBox;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.subviews.startpages.StartPage;
import de.amr.pacmanfx.ui.subviews.startpages.StartPages_SubView;
import de.amr.pacmanfx.ui.view.GameUI_MainScene;
import de.amr.pacmanfx.ui.view.GameUI_View_Implementation;
import de.amr.pacmanfx.ui.view.StatusIconBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static de.amr.pacmanfx.core.Validations.requireNonNegative;
import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring a {@link GameUI} instance.
 */
public class GameUI_Builder {

    record WindowConfig(Stage stage, int sceneWidth, int sceneHeight) {}

    record GameConfig(
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector) {}

    public static GameUI_Builder newUI(
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight,
        GameBox gameBox)
    {
        return new GameUI_Builder(stage, mainSceneWidth, mainSceneHeight, requireNonNull(gameBox));
    }

    private final WindowConfig windowConfig;
    private final GameBox gameBox;
    private final Map<String, GameConfig> gameConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();
    private boolean includeInteractiveTests;

    private GameUI_Builder(
        Stage stage,
        int mainSceneWidth,
        int mainSceneHeight,
        GameBox gameBox)
    {
        windowConfig = new WindowConfig(stage, mainSceneWidth, mainSceneHeight);
        this.gameBox = gameBox;
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
    public GameUI_Builder game(
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

    public GameUI_Builder game(
        String variantName,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory)
    {
        return game(variantName, gameModelFactory, uiConfigFactory, null);
    }

    public GameUI_Builder game(
        GameVariant variant,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory)
    {
        return game(variant.name(), gameModelFactory, uiConfigFactory, null);
    }

    public GameUI_Builder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public GameUI_Builder includeInteractiveTests(boolean include) {
        includeInteractiveTests = include;
        return this;
    }

    private GameUI_View_Implementation createViewImplementation(Stage stage, int width, int height) {
        return new GameUI_View_Implementation(
            stage,
            new GameUI_MainScene(requireNonNegative(width), requireNonNegative(height)),
            new StatusIconBox(() -> GameUI_Constants.LOCALIZED_TEXTS)
        );
    }

    public GameUI build() {
        validateConfigurationData();

        final var ui = new GameUI_Implementation(
            gameBox,
            createViewImplementation(windowConfig.stage(), windowConfig.sceneWidth(), windowConfig.sceneHeight())
        );

        gameConfigMap.forEach((gameVariant, config) -> {
            final AbstractGameModel game = config.gameModelFactory.get();
            gameBox.registerGame(gameVariant, game);
            ui.access().configurations().addConfigFactory(gameVariant, config.uiConfigFactory);
            if (includeInteractiveTests) {
                game.flow().addState(new LevelShortTestState<>(gameBox.coinMechanism()));
                game.flow().addState(new LevelMediumTestState<>());
                game.flow().addState(new CutScenesTestState<>());
            }
        });

        final StartPages_SubView startPagesCarousel = ui.access().subViews().startView();
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
        if (!de.amr.pacmanfx.core.GameBox.GAME_VARIANT_NAME_PATTERN.matcher(name).matches()) {
            error("Game variant name '%s' does not match pattern '%s'".formatted(name, de.amr.pacmanfx.core.GameBox.GAME_VARIANT_NAME_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
