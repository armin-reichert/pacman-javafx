/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.subviews.startpages.StartPage;
import de.amr.pacmanfx.ui.subviews.startpages.StartPagesView;
import de.amr.pacmanfx.ui.window.GameWindowImpl;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Builder for constructing and configuring a game application.
 */
public class GameBuilder {

    record WindowConfig(int sceneWidth, int sceneHeight) {}

    record GameVariantConfig(WorldMapSelector mapSelector) {}

    private final PacManGamesMachine machine;
    private final WindowConfig windowConfig;
    private final Map<String, GameVariantConfig> gameVariantConfigMap = new LinkedHashMap<>();
    private final List<Supplier<? extends StartPage>> startPageFactories = new ArrayList<>();

    public GameBuilder(PacManGamesMachine machine, int mainSceneWidth, int mainSceneHeight) {
        this.machine = requireNonNull(machine);
        windowConfig = new WindowConfig(mainSceneWidth, mainSceneHeight);
    }

    public GameBuilder worldMapSelector(GameVariantID gameVariantID, WorldMapSelector mapSelector) {
        validateGameVariantName(gameVariantID.name());
        gameVariantConfigMap.put(gameVariantID.name(), new GameVariantConfig(mapSelector));
        return this;
    }

    public GameBuilder startPage(Supplier<? extends StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory is null");
        }
        startPageFactories.add(startPageFactory);
        return this;
    }

    public Game build(Stage stage) {
        requireNonNull(stage);
        validateConfigurationData();

        final var game = new GameImpl(machine, new GameWindowImpl(stage, windowConfig.sceneWidth(), windowConfig.sceneHeight()));

        final StartPagesView startPagesView = game.ui().subViews().startView();
        for (var factory : startPageFactories) {
            final StartPage page = factory.get();
            if (page != null) {
                startPagesView.addStartPage(page);
            }
            else {
                error("Start page could not be created using factory: " + factory);
            }
        }

        //TODO Find better solution for shared world map selector
        gameVariantConfigMap.forEach((variantName, config) -> {
            if (config.mapSelector() != null) {
                game.gameVariant(variantName).gameModel().setMapSelector(config.mapSelector());
            }
        });

        return game;
    }

    private void validateConfigurationData() {
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
