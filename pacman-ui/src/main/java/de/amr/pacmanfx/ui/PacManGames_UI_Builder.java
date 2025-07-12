/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.ui.PacManGames_UI_Impl.THE_ONE;
import static java.util.Objects.requireNonNull;

public class PacManGames_UI_Builder {

    private static void checkUserDirsExistingAndWritable(GameContext gameContext) {
        File homeDir = gameContext.theHomeDir();
        File customMapDir = gameContext.theCustomMapDir();
        String homeDirDesc = "Pac-Man FX home directory";
        String customMapDirDesc = "Pac-Man FX custom map directory";
        boolean success = checkDirExistingAndWritable(homeDir, homeDirDesc);
        if (success) {
            Logger.info(homeDirDesc + " is " + homeDir);
            success = checkDirExistingAndWritable(customMapDir, customMapDirDesc);
            if (success) {
                Logger.info(customMapDirDesc + " is " + customMapDir);
            }
            Logger.info("User directories exist and are writable!");
        }
    }

    private static boolean checkDirExistingAndWritable(File dir, String description) {
        requireNonNull(dir);
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

    private final GameContext gameContext;
    private final Map<String, GameModel> models = new HashMap<>();
    private final Map<String, Class<? extends PacManGames_UIConfig>> uiConfigClasses = new HashMap<>();
    private StartPage[] startPages;
    private int selectedStartPageIndex;
    private DashboardID[] dashboardIDs = new DashboardID[0];

    PacManGames_UI_Builder(GameContext gameContext) {
        this.gameContext = requireNonNull(gameContext);
    }

    public PacManGames_UI_Builder game(String variant, GameModel model, Class<? extends PacManGames_UIConfig> configClass) {
        models.put(variant, model);
        uiConfigClasses.put(variant, configClass);
        return this;
    }

    public PacManGames_UI_Builder startPages(StartPage... startPages) {
        this.startPages = startPages;
        return this;
    }

    public PacManGames_UI_Builder selectStartPage(int index) {
        this.selectedStartPageIndex = index;
        return this;
    }

    public PacManGames_UI_Builder dashboardEntries(DashboardID... dashboardIDs) {
        this.dashboardIDs = dashboardIDs;
        return this;
    }

    public GameUI build() {
        validateConfiguration();
        THE_ONE.theWatchdog = new DirectoryWatchdog(gameContext.theCustomMapDir());
        checkUserDirsExistingAndWritable(gameContext);
        THE_ONE.configure(uiConfigClasses);
        models.forEach((variant, model) -> gameContext.theGameController().registerGame(variant, model));
        gameContext.theGameController().setEventsEnabled(true);
        THE_ONE.gameView().dashboard().configure(dashboardIDs);
        for (StartPage startPage : startPages) THE_ONE.startPagesView().addStartPage(startPage);
        THE_ONE.startPagesView().selectStartPage(selectedStartPageIndex);
        THE_ONE.startPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(gameContext.theGameController()::selectGameVariant);
        return THE_ONE;
    }

    private void validateConfiguration() {
        if (models.isEmpty()) {
            error("No game models specified");
        }
        models.forEach((variant, model) -> {
            validateGameVariant(variant);
            if (model == null) {
                error("Game model is null");
            }
        });
        if (uiConfigClasses.isEmpty()) {
            error("No UI configurations specified");
        }
        uiConfigClasses.forEach((variant, configClass) -> {
            validateGameVariant(variant);
            if (configClass == null) {
                error("UI configuration class is null");
            }
        });
        if (dashboardIDs == null) {
            error("Dashboard entry list is null");
        }
        if (startPages == null) {
            error("Start pages list is null");
        }
        if (startPages.length == 0) {
            error("Start pages list is empty");
        }
        if (selectedStartPageIndex < 0 || selectedStartPageIndex >= startPages.length) {
            error("Selected start page index (%d) is out of range 0..%d".formatted(selectedStartPageIndex, startPages.length - 1));
        }
    }

    private void validateGameVariant(String variant) {
        if (variant == null) {
            error("Game variant is null");
        }
        if (variant.isBlank()) {
            error("Game variant is blank string");
        }
        if (!variant.matches("[a-zA-Z_$][a-zA-Z_$0-9]*")) {
            error("Game variant ('%s') is not a valid identifier".formatted(variant));
        }
    }

    private void error(String message) {
        throw new RuntimeException("Application building failed: %s".formatted(message));
    }
}
