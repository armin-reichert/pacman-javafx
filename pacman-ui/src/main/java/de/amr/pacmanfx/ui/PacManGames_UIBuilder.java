/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import de.amr.pacmanfx.ui.sound.PacManGames_SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static java.util.Objects.requireNonNull;

public class PacManGames_UIBuilder {

    public static String MS_PACMAN = "MS_PACMAN";
    public static String MS_PACMAN_TENGEN = "MS_PACMAN_TENGEN";
    public static String MS_PACMAN_XXL = "MS_PACMAN_XXL";
    public static String PACMAN = "PACMAN";
    public static String PACMAN_XXL = "PACMAN_XXL";

    private final Map<String, GameModel> models = new HashMap<>();
    private final Map<String, Class<? extends PacManGames_UIConfig>> uiConfigClasses = new HashMap<>();
    private StartPage[] startPages;
    private int selectedStartPageIndex;
    private DashboardID[] dashboardIDs = new DashboardID[0];
    private Stage stage;
    private int width = 800;
    private int height = 600;

    private PacManGames_UIBuilder() {}

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

    public static PacManGames_UIBuilder buildUI() {
        checkUserDirsExistingAndWritable();
        PacManGames.theAssets = new PacManGames_Assets();
        PacManGames.theClock = new GameClock();
        PacManGames.theSound = new PacManGames_SoundManager();
        return new PacManGames_UIBuilder();
    }

    public PacManGames_UIBuilder game(String variant, GameModel model, Class<? extends PacManGames_UIConfig> configClass) {
        validateGameVariant(variant);
        validateGameModel(model);
        validateUIConfigClass(configClass);
        models.put(variant, model);
        uiConfigClasses.put(variant, configClass);
        return this;
    }

    public PacManGames_UIBuilder startPages(StartPage... startPages) {
        validateStartPageList(startPages);
        this.startPages = startPages;
        return this;
    }

    public PacManGames_UIBuilder selectStartPage(int index) {
        // is checked after start page list has been specified
        this.selectedStartPageIndex = index;
        return this;
    }

    public PacManGames_UIBuilder dashboardEntries(DashboardID... dashboardIDs) {
        validateDashboardEntryList(dashboardIDs);
        this.dashboardIDs = dashboardIDs;
        return this;
    }

    public PacManGames_UIBuilder stage(Stage stage, int width, int height) {
        validateStage(stage, width, height);
        this.stage = stage;
        this.width = width;
        this.height = height;
        return this;
    }

    public void show() {
        validateAll();
        var ui = new PacManGames_UI_Impl(uiConfigClasses);
        ui.buildUI(stage, width, height, dashboardIDs);
        models.forEach((variant, model) -> theGameController().registerGame(variant, model));
        theGameController().setEventsEnabled(true);
        for (StartPage startPage : startPages) ui.startPagesView().addStartPage(startPage);
        ui.startPagesView().selectStartPage(selectedStartPageIndex);
        ui.startPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(theGameController()::selectGameVariant);
        theUI = ui;
        theUI.show();
    }

    private void validateAll() {
        validateGameModels();
        validateDashboardEntryList(dashboardIDs);
        validateStartPageList(startPages);
        if (selectedStartPageIndex < 0 || selectedStartPageIndex >= startPages.length) {
            error("Selected start page index (%d) is out of range 0..%d".formatted(selectedStartPageIndex, startPages.length - 1));
        }
        validateStage(stage, width, height);
    }

    private void validateGameModels() {
        if (models.isEmpty()) {
            error("No game models specified");
        }
        models.forEach((variant, model) -> {
            validateGameVariant(variant);
            validateGameModel(model);
        });
        if (uiConfigClasses.isEmpty()) {
            error("No UI configurations specified");
        }
        uiConfigClasses.forEach((variant, configClass) -> {
            validateGameVariant(variant);
            validateUIConfigClass(configClass);
        });
    }

    private void validateGameModel(GameModel model) {
        if (model == null) {
            error("Game model is null");
        }
    }

    private void validateUIConfigClass(Class<?> configClass) {
        if (configClass == null) {
            error("UI configuration class is null");
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

    private void validateStartPageList(StartPage[] startPages) {
        if (startPages == null) {
            error("Start pages list is null");
        }
        if (startPages.length == 0) {
            error("Start pages list is empty");
        }
    }

    private void validateDashboardEntryList(DashboardID[] dashboardIDs) {
        if (dashboardIDs == null) {
            error("Dashboard entry list is null");
        }
    }

    private void validateStage(Stage stage, int width, int height) {
        if (stage == null) {
            error("Stage is null");
        }
        if (width <= 0) {
            error("Stage width (%d) must be a positive number".formatted(width));
        }
        if (height <= 0) {
            error("Stage height (%d) must be a positive number".formatted(height));
        }
    }

    private void error(String message) {
        throw new RuntimeException("Application building failed: %s".formatted(message));
    }
}
