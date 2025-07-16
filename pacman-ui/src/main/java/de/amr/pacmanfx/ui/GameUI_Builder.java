/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.MapSelector;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class GameUI_Builder {

    private final PacManGames_UI_Impl uiUnderConstruction;
    private final Map<String, Class<?>> gameModelClassByVariantName = new HashMap<>();
    private final Map<String, MapSelector> mapSelectorByVariantName = new HashMap<>();
    private final Map<String, Class<? extends GameUI_Config>> configClassByVariantName = new HashMap<>();
    private StartPage[] startPages;
    private DashboardID[] dashboardIDs = new DashboardID[0];

    public GameUI_Builder(PacManGames_UI_Impl uiUnderConstruction) {
        this.uiUnderConstruction = requireNonNull(uiUnderConstruction);
    }

    public GameUI_Builder game(String variant, Class<? extends GameModel> gameModelClass, Class<? extends GameUI_Config> configClass) {
        validateGameVariant(variant);
        if (gameModelClass == null) {
            error("Game model class for variant %s may not be null".formatted(variant));
        }
        if (configClass == null) {
            error("Game UI configuration class for variant %s may not be null".formatted(variant));
        }
        gameModelClassByVariantName.put(variant, gameModelClass);
        configClassByVariantName.put(variant, configClass);
        return this;
    }

    public GameUI_Builder game(
            String variant,
            Class<? extends GameModel> gameModelClass,
            MapSelector mapSelector,
            Class<? extends GameUI_Config> configClass)
    {
        validateGameVariant(variant);
        if (gameModelClass == null) {
            error("Game model class for variant %s may not be null".formatted(variant));
        }
        if (configClass == null) {
            error("Game UI configuration class for variant %s may not be null".formatted(variant));
        }
        if (mapSelector == null) {
            error("Map selector for variant %s may not be null".formatted(variant));
        }
        gameModelClassByVariantName.put(variant, gameModelClass);
        mapSelectorByVariantName.put(variant, mapSelector);
        configClassByVariantName.put(variant, configClass);
        return this;
    }

    public GameUI_Builder startPages(StartPage... startPages) {
        if (startPages == null) {
            error("Start pages list must not be null");
        }
        if (startPages.length == 0) {
            error("Start pages list must not be empty");
        }
        this.startPages = startPages;
        return this;
    }

    public GameUI_Builder dashboardEntries(DashboardID... dashboardIDs) {
        if (dashboardIDs == null) {
            error("Dashboard entry list must not be null");
        }
        this.dashboardIDs = dashboardIDs;
        return this;
    }

    public GameUI build() {
        GameContext gameContext = uiUnderConstruction.theGameContext();
        validateConfiguration(gameContext);
        uiUnderConstruction.configure(configClassByVariantName);
        gameModelClassByVariantName.keySet().forEach(variant -> {
            Class<?> gameModelClass = gameModelClassByVariantName.get(variant);
            MapSelector mapSelector = mapSelectorByVariantName.get(variant);
            GameModel gameModel =  createGameModel(gameModelClass, mapSelector, gameContext);
            gameContext.theGameController().registerGame(variant, gameModel);

        });
        gameContext.theGameController().setEventsEnabled(true);
        uiUnderConstruction.thePlayView().dashboard().configure(dashboardIDs);
        for (StartPage startPage : startPages) uiUnderConstruction.theStartPagesView().addStartPage(startPage);
        uiUnderConstruction.theStartPagesView().selectStartPage(0);
        uiUnderConstruction.theStartPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(gameContext.theGameController()::selectGameVariant);
        return uiUnderConstruction;
    }

    private GameModel createGameModel(Class<?> modelClass, MapSelector mapSelector, GameContext gameContext) {
        try {
            return (GameModel) (mapSelector != null
                ? modelClass.getDeclaredConstructor(GameContext.class, MapSelector.class).newInstance(gameContext, mapSelector)
                : modelClass.getDeclaredConstructor(GameContext.class).newInstance(gameContext));
        } catch (Exception x) {
            error("Could not create game model from class %s".formatted(modelClass.getSimpleName()));
            throw new RuntimeException(x);
        }
    }

    private void validateConfiguration(GameContext gameContext) {
        if (gameContext == null) {
            error("The game context must not be null");
        }
        checkDirsExistingAndWritable(gameContext);
        if (gameModelClassByVariantName.isEmpty()) {
            error("No game models specified");
        }
    }

    private void validateGameVariant(String variant) {
        if (variant == null) {
            error("Game variant must not be null");
        }
        if (variant.isBlank()) {
            error("Game variant must not be a blank string");
        }
        if (!variant.matches("[a-zA-Z_$][a-zA-Z_$0-9]*")) {
            error("Game variant ('%s') is not a valid identifier".formatted(variant));
        }
    }

    private void checkDirsExistingAndWritable(GameContext gameContext) {
        File homeDir = gameContext.theHomeDir();
        if (homeDir == null) {
            error("Home directory must not be null");
        }
        String homeDirDesc = "Pac-Man JavaFX home directory";
        File customMapDir = gameContext.theCustomMapDir();
        if (customMapDir == null) {
            error("Custom map directory must not be null");
        }
        String customMapDirDesc = "Pac-Man JavaFX custom map directory";
        boolean homeDirOK = ensureDirExistingAndWritable(homeDir, homeDirDesc);
        if (homeDirOK) {
            Logger.info("{} exists and is writable: {}", homeDirDesc, homeDir);
            boolean customMapDirOK = ensureDirExistingAndWritable(customMapDir, customMapDirDesc);
            if (customMapDirOK) {
                Logger.info("{} exists and is writable: {}", customMapDirDesc, customMapDir);
            }
        }
    }

    private boolean ensureDirExistingAndWritable(File dir, String description) {
        if (!dir.exists()) {
            Logger.info(description + " does not exist, create it...");
            if (!dir.mkdirs()) {
                Logger.error(description + " could not be created");
                return false;
            }
            Logger.info(description + " has been created");
            if (!dir.canWrite()) {
                Logger.error(description + " is not writable");
                return false;
            }
        }
        return true;
    }

    private void error(String message) {
        throw new RuntimeException("Application building failed: %s".formatted(message));
    }
}
