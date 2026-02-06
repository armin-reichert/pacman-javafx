/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.lib.fsm.StateMachine;
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

import static java.util.Objects.requireNonNull;

public class GameUI_Builder {

    private record WindowData(Stage stage, double sceneWidth, double sceneHeight) {}

    private record Configuration(
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector) {}

    public static GameUI_Builder newUI(Stage stage, double mainSceneWidth, double mainSceneHeight, GameBox gameBox) {
        return new GameUI_Builder(stage, mainSceneWidth, mainSceneHeight, requireNonNull(gameBox));
    }

    private final GameBox gameBox;
    private final WindowData windowData;
    private final Map<String, Configuration> configByGameVariant = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();
    private List<CommonDashboardID> dashboardIDs = List.of();
    private boolean includeTests;

    private GameUI_Builder(Stage stage, double mainSceneWidth, double mainSceneHeight, GameBox gameBox) {
        windowData = new WindowData(stage, mainSceneWidth, mainSceneHeight);
        this.gameBox = gameBox;
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
        final Configuration configuration = new Configuration(gameModelFactory, uiConfigFactory, optionalMapSelector);
        configByGameVariant.put(variantName, configuration);
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

    public GameUI_Builder includeTests(boolean include) {
        includeTests = include;
        return this;
    }

    public GameUI build() {
        validateConfiguration();

        final var ui = new GameUI_Implementation(gameBox, windowData.stage(), windowData.sceneWidth, windowData.sceneHeight);

        configByGameVariant.forEach((gameVariant, config) -> {
            final AbstractGameModel gameModel = config.gameModelFactory.get();
            gameBox.registerGame(gameVariant, gameModel);
            ui.uiConfigManager().addFactory(gameVariant, config.uiConfigFactory);
            if (includeTests) {
                final StateMachine<Game> gameStateMachine = gameModel.control().stateMachine();
                gameStateMachine.addState(new LevelShortTestState());
                gameStateMachine.addState(new LevelMediumTestState());
                gameStateMachine.addState(new CutScenesTestState());
            }
        });

        for (var startPageFactory : startPageFactories) {
            final StartPage startPage = startPageFactory.get();
            if (startPage != null) {
                ui.views().getStartPagesView().addStartPage(startPage);
                startPage.init(ui);
            }
            else {
                error("Start page could not be created");
            }
        }

        ui.dashboard().addCommonSections(ui, dashboardIDs);

        return ui;
    }

    private void validateConfiguration() {
        if (configByGameVariant.isEmpty()) {
            error("No game configuration specified");
        }
        if (windowData.sceneWidth() <= 0) {
            error("Main scene width must be a positive number");
        }
        if (windowData.sceneHeight() <= 0) {
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
