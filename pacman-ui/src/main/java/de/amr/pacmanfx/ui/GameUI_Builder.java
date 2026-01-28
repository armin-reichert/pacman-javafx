/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import javafx.stage.Stage;

import java.util.*;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static java.util.Objects.requireNonNull;

public class GameUI_Builder {

    private static class GameConfiguration {
        Supplier<? extends AbstractGameModel> gameModelFactory;
        Supplier<? extends UIConfig> uiConfigFactory;
        WorldMapSelector mapSelector;
    }

    public static GameUI_Builder newUI(Stage stage, double width, double height) {
        return new GameUI_Builder(stage, width, height);
    }

    private final Stage stage;
    private final double mainSceneWidth;
    private final double mainSceneHeight;
    private final Map<String, GameConfiguration> configByGameVariant = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();
    private List<CommonDashboardID> dashboardIDs = List.of();

    private GameUI_Builder(Stage stage, double width, double height) {
        this.stage = requireNonNull(stage);
        this.mainSceneWidth = width;
        this.mainSceneHeight = height;
    }

    private GameConfiguration configuration(String gameVariant) {
        if (!configByGameVariant.containsKey(gameVariant)) {
            configByGameVariant.put(gameVariant, new GameConfiguration());
        }
        return configByGameVariant.get(gameVariant);
    }

    public GameUI_Builder game(
        String variantName,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector optionalMapSelector)
    {
        validateGameVariantName(variantName);
        if (gameModelFactory == null) {
            error("Game model factory for game variant '%s' is null".formatted(variantName));
        }
        if (uiConfigFactory == null) {
            error("Game UI configuration factory for game variant '%s' is null".formatted(variantName));
        }
        configuration(variantName).gameModelFactory = gameModelFactory;
        configuration(variantName).uiConfigFactory = uiConfigFactory;
        configuration(variantName).mapSelector = optionalMapSelector;
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
            error("Start page factory must not be null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public GameUI_Builder dashboard(CommonDashboardID... dashboardIDs) {
        if (dashboardIDs == null) {
            error("Dashboard entry list must not be null");
        }
        this.dashboardIDs = Arrays.asList(dashboardIDs);
        return this;
    }

    public GameUI build() {
        validateConfiguration();

        var ui = new GameUI_Implementation(THE_GAME_BOX, stage, mainSceneWidth, mainSceneHeight);

        configByGameVariant.forEach((gameVariant, config) -> {
            Game game = config.gameModelFactory.get();
            //TODO make configurable
            game.control().stateMachine().addState(new LevelShortTestState());
            game.control().stateMachine().addState(new LevelMediumTestState());
            game.control().stateMachine().addState(new CutScenesTestState());
            THE_GAME_BOX.registerGame(gameVariant, game);
        });

        configByGameVariant.forEach((gameVariant, config) -> ui.uiConfigManager().addFactory(gameVariant, config.uiConfigFactory));

        for (var factory : startPageFactories) {
            StartPage startPage = factory.get();
            if (startPage != null) {
                ui.views().startPagesView().addStartPage(startPage);
                startPage.init(ui);
            }
        }

        ui.views().playView().dashboard().addCommonSections(ui, dashboardIDs);
        return ui;
    }

    private void validateConfiguration() {
        if (configByGameVariant.isEmpty()) {
            error("No game configuration specified");
        }
        if (mainSceneWidth <= 0) {
            error("Main scene width must be a positive number");
        }
        if (mainSceneHeight <= 0) {
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
        if (!GameBox.GAME_VARIANT_NAME_PATTERN.matcher(name).matches()) {
            error("Game variant name '%s' does not match pattern '%s'".formatted(name, GameBox.GAME_VARIANT_NAME_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
