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
import java.util.*;

public class GameUI_Builder {

    public static GameUI_Builder createUI(Stage stage, double width, double height) {
        PacManGames_UI_Impl.THE_ONE = new PacManGames_UI_Impl(Globals.theGameContext(), stage, width, height);
        return new GameUI_Builder(PacManGames_UI_Impl.THE_ONE);
    }

    private final PacManGames_UI_Impl ui;
    private final Map<String, Class<?>> gameModelClassByVariant = new HashMap<>();
    private final Map<String, MapSelector> mapSelectorByVariant = new HashMap<>();
    private final Map<String, Class<? extends GameUI_Config>> uiConfigClassByVariant = new HashMap<>();
    private List<StartPage> startPages = new ArrayList<>();
    private List<DashboardID> dashboardIDs = new ArrayList<>();

    private GameUI_Builder(PacManGames_UI_Impl ui) {
        this.ui = ui;
    }

    public GameUI_Builder game(
        String variant,
        Class<? extends GameModel> gameModelClass,
        Class<? extends GameUI_Config> configClass)
    {
        validateGameVariantKey(variant);
        if (gameModelClass == null) {
            error("Game model class for variant %s may not be null".formatted(variant));
        }
        if (configClass == null) {
            error("Game UI configuration class for variant %s may not be null".formatted(variant));
        }
        gameModelClassByVariant.put(variant, gameModelClass);
        uiConfigClassByVariant.put(variant, configClass);
        return this;
    }

    public GameUI_Builder game(
            String variant,
            Class<? extends GameModel> gameModelClass,
            MapSelector mapSelector,
            Class<? extends GameUI_Config> configClass)
    {
        validateGameVariantKey(variant);
        if (gameModelClass == null) {
            error("Game model class for variant %s may not be null".formatted(variant));
        }
        if (configClass == null) {
            error("Game UI configuration class for variant %s may not be null".formatted(variant));
        }
        if (mapSelector == null) {
            error("Map selector for variant %s may not be null".formatted(variant));
        }
        gameModelClassByVariant.put(variant, gameModelClass);
        mapSelectorByVariant.put(variant, mapSelector);
        uiConfigClassByVariant.put(variant, configClass);
        return this;
    }

    public GameUI_Builder startPages(StartPage... startPages) {
        if (startPages == null) {
            error("Start pages list must not be null");
        }
        if (startPages.length == 0) {
            error("Start pages list must not be empty");
        }
        this.startPages = Arrays.asList(startPages);
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
        ui.configure(uiConfigClassByVariant);
        gameModelClassByVariant.keySet().forEach(gameVariant -> {
            Class<?> gameModelClass = gameModelClassByVariant.get(gameVariant);
            MapSelector mapSelector = mapSelectorByVariant.get(gameVariant);
            GameModel gameModel = createGameModel(
                gameModelClass,
                mapSelector,
                ui.theGameContext(),
                highScoreFile(ui.theGameContext().theHomeDir(), gameVariant));
            ui.theGameContext().theGameController().registerGame(gameVariant, gameModel);
        });
        ui.thePlayView().dashboard().configure(dashboardIDs);
        startPages.forEach(startPage -> ui.theStartPagesView().addStartPage(startPage));
        ui.theStartPagesView().selectStartPage(0);
        ui.theStartPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(ui.theGameContext().theGameController()::selectGameVariant);

        ui.theGameContext().theGameController().setEventsEnabled(true);
        return ui;
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
        if (gameModelClassByVariant.isEmpty()) {
            error("No game models specified");
        }
    }

    private void validateGameVariantKey(String gameVariantKey) {
        if (gameVariantKey == null) {
            error("Game variant key must not be null");
        }
        if (gameVariantKey.isBlank()) {
            error("Game variant key must not be a blank string");
        }
        if (!GameController.GAME_VARIANT_PATTERN.matcher(gameVariantKey).matches()) {
            error("Game variant key '%s' does not match required syntax '%s'"
                .formatted(gameVariantKey, GameController.GAME_VARIANT_PATTERN));
        }
    }

    private void error(String message) {
        throw new RuntimeException("Application building failed: %s".formatted(message));
    }
}
