/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.model.test.LevelMediumTestState;
import de.amr.pacmanfx.model.test.LevelShortTestState;
import de.amr.pacmanfx.model.world.WorldMapSelector;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
import static java.util.Objects.requireNonNull;

public class GameUI_Builder {

    private static class GameConfiguration {
        Class<?> gameModelClass;
        Supplier<? extends GameUI_Config> uiConfigFactory;
        WorldMapSelector mapSelector;
    }

    private static class StartPageConfiguration {
        Supplier<? extends GameUI_StartPage> startPageFactory;
    }

    public static GameUI_Builder create(Stage stage, double width, double height) {
        return new GameUI_Builder(stage, width, height);
    }

    private final Stage stage;
    private final double mainSceneWidth;
    private final double mainSceneHeight;
    private final Map<String, GameConfiguration> configByGameVariant = new LinkedHashMap<>();
    private final List<StartPageConfiguration> startPageConfigs = new ArrayList<>();
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
        String variant,
        Class<? extends Game> gameModelClass,
        Supplier<? extends GameUI_Config> uiConfigFactory)
    {
        validateGameVariantName(variant);
        if (gameModelClass == null) {
            error("Game model class for game variant '%s' is null".formatted(variant));
        }
        if (uiConfigFactory == null) {
            error("Game UI configuration factory for game variant '%s' is null".formatted(variant));
        }
        configuration(variant).gameModelClass = gameModelClass;
        configuration(variant).uiConfigFactory = uiConfigFactory;
        return this;
    }

    public GameUI_Builder game(
        String variant,
        Class<? extends Game> gameModelClass,
        WorldMapSelector mapSelector,
        Supplier<? extends GameUI_Config> uiConfigFactory)
    {
        validateGameVariantName(variant);
        if (gameModelClass == null) {
            error("Game model class for game variant '%s' is null".formatted(variant));
        }
        if (uiConfigFactory == null) {
            error("Game UI configuration factory for game variant '%s' is null".formatted(variant));
        }
        if (mapSelector == null) {
            error("Map selector for variant %s may not be null".formatted(variant));
        }
        configuration(variant).gameModelClass = gameModelClass;
        configuration(variant).uiConfigFactory = uiConfigFactory;
        configuration(variant).mapSelector = mapSelector;
        return this;
    }

    public GameUI_Builder startPage(Supplier<? extends GameUI_StartPage> startPageFactory) {
        if (startPageFactory == null) {
            error("Start page factory must not be null");
        }
        var startPageConfig = new StartPageConfiguration();
        startPageConfig.startPageFactory = startPageFactory;
        startPageConfigs.add(startPageConfig);
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
            File highScoreFile = GameBox.highScoreFile(gameVariant);
            Game game = createGame(config.gameModelClass, config.mapSelector, highScoreFile);
            //TODO make configurable
            game.control().stateMachine().addState(new LevelShortTestState());
            game.control().stateMachine().addState(new LevelMediumTestState());
            game.control().stateMachine().addState(new CutScenesTestState());
            THE_GAME_BOX.registerGame(gameVariant, game);
        });

        configByGameVariant.forEach((gameVariant, config) -> ui.configFactory().addFactory(gameVariant, config.uiConfigFactory));

        for (StartPageConfiguration startPageConfig : startPageConfigs) {
            GameUI_StartPage startPage = createStartPage(startPageConfig.startPageFactory);
            if (startPage != null) {
                ui.views().startPagesView().addStartPage(startPage);
                startPage.init(ui);
            }
        }

        ui.views().playView().dashboard().addCommonSections(ui, dashboardIDs);
        return ui;
    }

    private GameUI_StartPage createStartPage(Supplier<? extends GameUI_StartPage> startPageFactory) {
        try {
            return startPageFactory.get();
        } catch (Exception x) {
            error("Could not create start page");
            return null;
        }
    }

    private AbstractGameModel createGame(Class<?> modelClass, WorldMapSelector mapSelector, File highScoreFile) {
        AbstractGameModel game = null;
        try {
            if (mapSelector != null) {
                game = (AbstractGameModel) modelClass
                    .getDeclaredConstructor(CoinMechanism.class, WorldMapSelector.class, File.class)
                    .newInstance(THE_GAME_BOX, mapSelector, highScoreFile);
            }
            else {
                game = (AbstractGameModel) modelClass
                    .getDeclaredConstructor(CoinMechanism.class, File.class)
                    .newInstance(THE_GAME_BOX, highScoreFile);
            }
        } catch (Exception x) {
            Logger.info("1st try: Could not create game model '{}'", modelClass.getSimpleName());
        }
        if (game == null) {
            try {
                game = (AbstractGameModel) modelClass
                    .getDeclaredConstructor(File.class)
                    .newInstance(highScoreFile);
            } catch (Exception x) {
                Logger.info("2nd try: Could not create game model '{}'", modelClass.getSimpleName());
                Logger.info(x);
            }
        }
        if (game != null) {
            Logger.info("Game model '{} created", modelClass.getSimpleName());
            return game;
        }
        throw new RuntimeException("Giving up: Could not create game model '%s'".formatted(modelClass.getSimpleName()));
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
