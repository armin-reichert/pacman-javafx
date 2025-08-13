/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class GameUI_Builder {

    private static class GameConfiguration {
        Class<?> gameModelClass;
        Class<?> uiConfigClass;
        MapSelector mapSelector;
    }

    private static class StartPageConfiguration {
        Class<?> startPageClass;
        List<String> gameVariants;
    }

    public static GameUI_Builder createUI(Stage stage, double width, double height) {
        Logger.info("JavaFX runtime: {}", System.getProperty("javafx.runtime.version"));
        return new GameUI_Builder(stage, width, height);
    }

    private final Stage stage;
    private final double mainSceneWidth;
    private final double mainSceneHeight;
    private final Map<String, GameConfiguration> configByGameVariant = new LinkedHashMap<>();
    private final List<StartPageConfiguration> startPageConfigs = new ArrayList<>();
    private List<DashboardID> dashboardIDs = List.of();

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
        Class<? extends GameUI_Config> uiConfigClass)
    {
        validateGameVariantKey(variant);
        if (gameModelClass == null) {
            error("Game model class for variant %s may not be null".formatted(variant));
        }
        if (uiConfigClass == null) {
            error("Game UI configuration class for variant %s may not be null".formatted(variant));
        }
        configuration(variant).gameModelClass = gameModelClass;
        configuration(variant).uiConfigClass = uiConfigClass;
        return this;
    }

    public GameUI_Builder game(
            String variant,
            Class<? extends Game> gameModelClass,
            MapSelector mapSelector,
            Class<? extends GameUI_Config> uiConfigClass)
    {
        validateGameVariantKey(variant);
        if (gameModelClass == null) {
            error("Game model class for variant %s may not be null".formatted(variant));
        }
        if (uiConfigClass == null) {
            error("Game UI configuration class for variant %s may not be null".formatted(variant));
        }
        if (mapSelector == null) {
            error("Map selector for variant %s may not be null".formatted(variant));
        }
        configuration(variant).gameModelClass = gameModelClass;
        configuration(variant).uiConfigClass = uiConfigClass;
        configuration(variant).mapSelector = mapSelector;
        return this;
    }

    public GameUI_Builder startPage(Class<?> startPageClass, String... gameVariants) {
        if (startPageClass == null) {
            error("Start page class must not be null");
        }
        if (gameVariants == null) {
            error("Game variants list must not be null");
        }
        var config = new StartPageConfiguration();
        config.startPageClass = startPageClass;
        config.gameVariants = List.of(gameVariants);
        startPageConfigs.add(config);
        return this;
    }

    public GameUI_Builder dashboard(DashboardID... dashboardIDs) {
        if (dashboardIDs == null) {
            error("Dashboard entry list must not be null");
        }
        this.dashboardIDs = Arrays.asList(dashboardIDs);
        return this;
    }

    public GameUI build() {
        validateConfiguration();

        final GameContext gameContext = Globals.theGameContext();

        //TODO this is crap
        Map<String, Class<?>> uiConfigMap = new HashMap<>();
        configByGameVariant.forEach((gameVariant, config) -> uiConfigMap.put(gameVariant, config.uiConfigClass));
        var ui = new GameUI_Implementation(uiConfigMap, gameContext, stage, mainSceneWidth, mainSceneHeight);

        configByGameVariant.forEach((gameVariant, config) -> {
            File highScoreFile = highScoreFile(gameContext.homeDir(), gameVariant);
            Game gameModel = createGameModel(config.gameModelClass, config.mapSelector, gameContext, highScoreFile);
            gameContext.gameController().registerGame(gameVariant, gameModel);
        });

        for (StartPageConfiguration config : startPageConfigs) {
            StartPage startPage = createStartPage(ui, config.gameVariants.getFirst(), config.startPageClass);
            ui.startPagesView().addStartPage(startPage);
        }

        ui.playView().configureDashboard(dashboardIDs);
        return ui;
    }

    private StartPage createStartPage(GameUI ui, String gameVariant, Class<?> startPageClass) {
        // first try constructor(GameUI, String)
        try {
            var constructor = startPageClass.getDeclaredConstructor(GameUI.class, String.class);
            return (StartPage) constructor.newInstance(ui, gameVariant);
        } catch (NoSuchMethodException x) {
            // then try constructor(GameUI)
            try {
                var constructor = startPageClass.getDeclaredConstructor(GameUI.class);
                return (StartPage) constructor.newInstance(ui);
            } catch (Exception xx) {
                error("Could not create start page from class '%s'".formatted(startPageClass.getSimpleName()));
                throw new IllegalStateException(xx);
            }
        } catch (Exception x) {
            error("Could not create start page from class '%s'".formatted(startPageClass.getSimpleName()));
            throw new IllegalStateException(x);
        }
    }

    private File highScoreFile(File dir, String gameVariant) {
        return new File(dir, "highscore-%s.xml".formatted(gameVariant).toLowerCase());
    }

    private Game createGameModel(Class<?> modelClass, MapSelector mapSelector, GameContext gameContext, File highScoreFile) {
        try {
            return (Game) (mapSelector != null
                ? modelClass.getDeclaredConstructor(GameContext.class, MapSelector.class, File.class).newInstance(gameContext, mapSelector, highScoreFile)
                : modelClass.getDeclaredConstructor(GameContext.class, File.class).newInstance(gameContext, highScoreFile));
        } catch (Exception x) {
            error("Could not create game model from class %s".formatted(modelClass.getSimpleName()));
            throw new RuntimeException(x);
        }
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

    private void validateGameVariantKey(String key) {
        if (key == null) {
            error("Game variant key must not be null");
        }
        if (key.isBlank()) {
            error("Game variant key must not be a blank string");
        }
        if (!GameController.GAME_VARIANT_PATTERN.matcher(key).matches()) {
            error("Game variant key '%s' does not match pattern '%s'".formatted(key, GameController.GAME_VARIANT_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
