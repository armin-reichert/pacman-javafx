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
import de.amr.pacmanfx.uilib.GameClockImpl;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring a {@link GameUI} instance.
 *
 * <p>This class provides a fluent API for assembling all components required
 * to launch the Pac‑Man FX user interface. It allows clients to register
 * multiple game variants, associate each variant with its own game model and
 * UI configuration, define start pages, configure dashboard sections, and
 * optionally enable interactive test modes.</p>
 *
 * <p>The builder separates concerns cleanly:</p>
 * <ul>
 *   <li><strong>Game variant registration</strong> – each variant is mapped to
 *       a factory for creating its {@link AbstractGameModel}, a factory for
 *       creating its {@link UIConfig}, and an optional {@link WorldMapSelector}.</li>
 *
 *   <li><strong>UI composition</strong> – start pages and dashboard sections
 *       are collected and added to the resulting {@link GameUI} during
 *       {@link #build()}.</li>
 *
 *   <li><strong>Optional interactive test states</strong> – when enabled via
 *       {@link #includeInteractiveTests(boolean)}, additional developer‑oriented states
 *       (cutscene tests, mid‑level starts, etc.) are injected into the game’s
 *       state machine. These states do not interfere with normal gameplay and
 *       can only be entered through explicit developer key combinations.</li>
 *
 *   <li><strong>Window configuration</strong> – the builder stores the primary
 *       JavaFX {@link Stage} and initial scene dimensions, ensuring the
 *       resulting UI is initialized with the correct window context.</li>
 * </ul>
 *
 * <p>The builder performs validation before constructing the UI to ensure that
 * all required components are present and that configuration errors are
 * detected early. The resulting {@link GameUI} is fully initialized, with all
 * registered game variants, start pages, dashboards, and optional test modes
 * ready for use.</p>
 *
 * <p>Instances of this builder are created via
 * {@link #newUI(Stage, int, int, GameBox)}, after which clients may
 * chain configuration calls before invoking {@link #build()} to obtain the
 * final UI.</p>
 */
public class GameUI_Builder {

    private record WindowConfig(Stage stage, int sceneWidth, int sceneHeight) {}

    private record GameConfig(
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector) {}

    public static GameUI_Builder newUI(Stage stage, int mainSceneWidth, int mainSceneHeight, GameBox gameBox) {
        return new GameUI_Builder(stage, mainSceneWidth, mainSceneHeight, requireNonNull(gameBox));
    }

    private final GameBox gameBox;
    private final WindowConfig windowConfig;
    private final Map<String, GameConfig> gameConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();
    private final List<CommonDashboardID> dashboardIDs = new ArrayList<>();
    private boolean includeInteractiveTests;

    private GameUI_Builder(Stage stage, int mainSceneWidth, int mainSceneHeight, GameBox gameBox) {
        windowConfig = new WindowConfig(stage, mainSceneWidth, mainSceneHeight);
        this.gameBox = gameBox;
        gameBox.setClock(new GameClockImpl());
    }

    public GameUI_Builder game(
        String variantName,
        Supplier<? extends AbstractGameModel> gameModelFactory,
        Supplier<? extends UIConfig> uiConfigFactory,
        WorldMapSelector mapSelector)
    {
        validateGameVariantName(variantName);
        if (gameModelFactory == null) {
            error("Game model factory for game variant '%s' is null".formatted(variantName));
        }
        if (uiConfigFactory == null) {
            error("UI configuration factory for game variant '%s' is null".formatted(variantName));
        }
        final GameConfig gameConfig = new GameConfig(gameModelFactory, uiConfigFactory, mapSelector);
        gameConfigMap.put(variantName, gameConfig);
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

    public GameUI_Builder dashboard(CommonDashboardID... ids) {
        this.dashboardIDs.addAll(List.of(ids));
        return this;
    }

    public GameUI_Builder includeInteractiveTests(boolean include) {
        includeInteractiveTests = include;
        return this;
    }

    public GameUI build() {
        validateConfigurationData();

        final var ui = new GameUI_Implementation(gameBox, windowConfig.stage(), windowConfig.sceneWidth(), windowConfig.sceneHeight());

        gameConfigMap.forEach((gameVariant, config) -> {
            final AbstractGameModel gameModel = config.gameModelFactory.get();
            gameBox.registerGame(gameVariant, gameModel);
            ui.uiConfigManager().addFactory(gameVariant, config.uiConfigFactory);
            if (includeInteractiveTests) {
                final StateMachine<Game> gameStateMachine = gameModel.control().stateMachine();
                gameStateMachine.addState(new LevelShortTestState(gameBox.coinMechanism()));
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
        if (!GameBox.GAME_VARIANT_NAME_PATTERN.matcher(name).matches()) {
            error("Game variant name '%s' does not match pattern '%s'".formatted(name, GameBox.GAME_VARIANT_NAME_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
