/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.controller.GameController;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameUI_Builder {

    private static class Configuration {
        Class<?> gameModelClass;
        MapSelector mapSelector;
        Class<?> uiConfigClass;
        Class<?> startPageClass;
    }

    public static GameUI_Builder createUI(Stage stage, double width, double height) {
        PacManGames_UI_Impl.THE_ONE = new PacManGames_UI_Impl(Globals.theGameContext(), stage, width, height);
        return new GameUI_Builder(PacManGames_UI_Impl.THE_ONE);
    }

    private final PacManGames_UI_Impl ui;
    private final Map<String, Configuration> configurationByGameVariant = new LinkedHashMap<>();
    private List<DashboardID> dashboardIDs = List.of();

    private GameUI_Builder(PacManGames_UI_Impl ui) {
        this.ui = ui;
    }

    private Configuration configuration(String gameVariant) {
        if (!configurationByGameVariant.containsKey(gameVariant)) {
            configurationByGameVariant.put(gameVariant, new Configuration());
        }
        return configurationByGameVariant.get(gameVariant);
    }

    public GameUI_Builder game(
        String variant,
        Class<? extends GameModel> gameModelClass,
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
            Class<? extends GameModel> gameModelClass,
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

    public GameUI_Builder startPage(String gameVariant, Class<?> startPageClass) {
        if (startPageClass == null) {
            error("Start page class must not be null");
        }
        configuration(gameVariant).startPageClass = startPageClass;
        return this;
    }

    public GameUI_Builder startPageShared(String gameVariant, String otherGameVariant) {
        Class<?> sharedStartPageClass = configuration(otherGameVariant).startPageClass;
        if (sharedStartPageClass == null) {
            error("Shared start page not found. Must define start page of shared game variant first!");
            throw new IllegalStateException();
        }
        configuration(gameVariant).startPageClass = sharedStartPageClass;
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
        configurationByGameVariant.keySet().forEach(gameVariant -> {
            Configuration configuration = configuration(gameVariant);
            GameModel gameModel = createGameModel(
                configuration.gameModelClass,
                configuration.mapSelector,
                ui.theGameContext(),
                highScoreFile(ui.theGameContext().theHomeDir(), gameVariant)
            );
            gameModel.init();
            ui.theGameContext().theGameController().registerGame(gameVariant, gameModel);
            ui.applyConfiguration(gameVariant, configuration.uiConfigClass);
            if (configuration.startPageClass != null) {
                StartPage startPage = createStartPage(gameVariant, configuration.startPageClass);
                ui.theStartPagesView().addStartPage(startPage);
            }
        });
        ui.thePlayView().dashboard().configure(dashboardIDs);

        ui.theStartPagesView().selectStartPage(0);
        ui.theStartPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(ui.theGameContext().theGameController()::selectGameVariant);

        ui.theGameContext().theGameController().setEventsEnabled(true);
        return ui;
    }

    private StartPage createStartPage(String gameVariant, Class<?> startPageClass) {
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
                throw new IllegalStateException();
            }
        } catch (Exception x) {
            error("Could not create start page from class '%s'".formatted(startPageClass.getSimpleName()));
            throw new IllegalStateException();
        }
    }

    private File highScoreFile(File dir, String gameVariant) {
        return new File(dir, "highscore-%s.xml".formatted(gameVariant).toLowerCase());
    }

    private GameModel createGameModel(Class<?> modelClass, MapSelector mapSelector, GameContext gameContext, File highScoreFile) {
        try {
            return (GameModel) (mapSelector != null
                ? modelClass.getDeclaredConstructor(GameContext.class, MapSelector.class, File.class).newInstance(gameContext, mapSelector, highScoreFile)
                : modelClass.getDeclaredConstructor(GameContext.class, File.class).newInstance(gameContext, highScoreFile));
        } catch (Exception x) {
            error("Could not create game model from class %s".formatted(modelClass.getSimpleName()));
            throw new RuntimeException(x);
        }
    }

    private void validateConfiguration() {
        if (configurationByGameVariant.isEmpty()) {
            error("No game configuration specified");
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
