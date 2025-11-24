/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.controller.CoinMechanism;
import de.amr.pacmanfx.controller.GameBox;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameStateMachine;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.api.StartPage;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.*;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;
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

        //TODO this is crap
        Map<String, Class<?>> uiConfigMap = new HashMap<>();
        configByGameVariant.forEach((gameVariant, config) -> uiConfigMap.put(gameVariant, config.uiConfigClass));
        var ui = new GameUI_Implementation(uiConfigMap, THE_GAME_BOX, stage, mainSceneWidth, mainSceneHeight);

        configByGameVariant.forEach((gameVariant, config) -> {
            File highScoreFile = highScoreFile(THE_GAME_BOX.homeDir(), gameVariant);
            Game gameModel = createGameModel(config.gameModelClass, config.mapSelector, highScoreFile);
            THE_GAME_BOX.registerGame(gameVariant, gameModel);
        });

        for (StartPageConfiguration config : startPageConfigs) {
            StartPage startPage = createStartPage(ui, config.gameVariants.getFirst(), config.startPageClass);
            ui.startPagesView().addStartPage(startPage);
        }

        ui.playView().dashboard().configure(dashboardIDs);
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
                error("Could not create start page from class '%s'".formatted(startPageClass.getSimpleName()), xx);
                throw new IllegalStateException(xx);
            }
        } catch (Exception x) {
            error("Could not create start page from class '%s'".formatted(startPageClass.getSimpleName()), x);
            throw new IllegalStateException(x);
        }
    }

    private File highScoreFile(File dir, String gameVariant) {
        return new File(dir, "highscore-%s.xml".formatted(gameVariant).toLowerCase());
    }

    private Game createGameModel(Class<?> modelClass, MapSelector mapSelector, File highScoreFile) {
        final CoinMechanism coinMechanism = THE_GAME_BOX.coinMechanism();
        AbstractGameModel game = null;
        try {
            if (mapSelector != null) {
                game = (AbstractGameModel) modelClass
                    .getDeclaredConstructor(CoinMechanism.class, MapSelector.class, File.class)
                    .newInstance(coinMechanism, mapSelector, highScoreFile);
            }
            else {
                game = (AbstractGameModel) modelClass
                    .getDeclaredConstructor(CoinMechanism.class, File.class)
                    .newInstance(coinMechanism, highScoreFile);
            }
        } catch (Exception x) {
            Logger.info("1st try: Could not create game model, class=%s".formatted(modelClass.getSimpleName()), x);
        }
        if (game == null) {
            try {
                game = (AbstractGameModel) modelClass
                    .getDeclaredConstructor(File.class)
                    .newInstance(highScoreFile);
            } catch (Exception x) {
                Logger.info("2nd try: Could not create game model, class=%s".formatted(modelClass.getSimpleName()), x);
            }
        }
        if (game != null) {
            game.setStateMachine(new GameStateMachine(THE_GAME_BOX, game));
            Logger.info("Success: Game model created, class={}", modelClass.getSimpleName());
            return game;
        }
        throw new RuntimeException("Giving up: Could not create game model");
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
        if (!GameBox.GAME_VARIANT_PATTERN.matcher(key).matches()) {
            error("Game variant key '%s' does not match pattern '%s'".formatted(key, GameBox.GAME_VARIANT_PATTERN));
        }
    }

    private void error(String message, Throwable x) {
        throw new RuntimeException("UI building failed: %s".formatted(message), x);
    }

    private void error(String message) {
        throw new RuntimeException("UI building failed: %s".formatted(message));
    }
}
