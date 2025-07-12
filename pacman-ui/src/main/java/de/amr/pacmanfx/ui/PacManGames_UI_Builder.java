/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.CUSTOM_MAP_DIR;
import static de.amr.pacmanfx.Globals.theGameContext;
import static java.util.Objects.requireNonNull;

public class PacManGames_UI_Builder {

    private static void checkUserDirsExistingAndWritable() {
        String homeDirDesc = "Pac-Man FX home directory";
        String customMapDirDesc = "Pac-Man FX custom map directory";
        boolean success = checkDirExistingAndWritable(Globals.HOME_DIR, homeDirDesc);
        if (success) {
            Logger.info(homeDirDesc + " is " + Globals.HOME_DIR);
            success = checkDirExistingAndWritable(Globals.CUSTOM_MAP_DIR, customMapDirDesc);
            if (success) {
                Logger.info(customMapDirDesc + " is " + Globals.CUSTOM_MAP_DIR);
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

    private final Stage stage;
    private final double width;
    private final double height;
    private final Map<String, GameModel> models = new HashMap<>();
    private final Map<String, Class<? extends PacManGames_UIConfig>> uiConfigClasses = new HashMap<>();
    private StartPage[] startPages;
    private int selectedStartPageIndex;
    private DashboardID[] dashboardIDs = new DashboardID[0];

    PacManGames_UI_Builder(Stage stage, double width, double height) {
        this.stage = stage;
        this.width = width;
        this.height = height;
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

    public PacManGames_UI build() {
        validate();
        final var ui = new PacManGames_UI_Impl(stage, width, height);
        ui.configure(uiConfigClasses);
        models.forEach((variant, model) -> theGameContext().theGameController().registerGame(variant, model));
        theGameContext().theGameController().setEventsEnabled(true);
        ui.gameView().dashboard().configure(dashboardIDs);
        for (StartPage startPage : startPages) ui.startPagesView().addStartPage(startPage);
        ui.startPagesView().selectStartPage(selectedStartPageIndex);
        ui.startPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(theGameContext().theGameController()::selectGameVariant);
        PacManGames_UI_Impl.THE_ONE = ui;
        return PacManGames_UI_Impl.THE_ONE;
    }

    private void validate() {
        checkUserDirsExistingAndWritable();
        PacManGames_UI_Impl.WATCHDOG = new DirectoryWatchdog(CUSTOM_MAP_DIR);
        if (stage == null) {
            error("Stage is null");
        }
        if (width <= 0) {
            error("Stage width (%.2f) must be a positive number".formatted(width));
        }
        if (height <= 0) {
            error("Stage height (%.2f) must be a positive number".formatted(height));
        }
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
