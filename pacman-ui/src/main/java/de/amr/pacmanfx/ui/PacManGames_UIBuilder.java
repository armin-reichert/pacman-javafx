/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

public class PacManGames_UIBuilder {

    private final Map<String, GameModel> models = new HashMap<>();
    private final Map<String, Class<? extends PacManGames_UIConfig>> configs = new HashMap<>();
    private StartPage[] startPages;
    private int selectedStartPageIndex;
    private DashboardID[] dashboardIDs;
    private Stage stage;
    private int width = 800;
    private int height = 600;

    private void error(String message) {
        throw new RuntimeException("Application building failed: %s".formatted(message));
    }

    private PacManGames_UIBuilder() {
        PacManGames_Env.init();
    }

    public static PacManGames_UIBuilder buildUI() {
        return new PacManGames_UIBuilder();
    }

    public PacManGames_UIBuilder game(String variant, GameModel model, Class<? extends PacManGames_UIConfig> configClass) {
        if (variant == null) {
            error("Game variant is null");
        }
        if (configClass == null) {
            error("UI configuration class is null");
        }
        models.put(variant, model);
        configs.put(variant, configClass);
        return this;
    }

    public PacManGames_UIBuilder startPages(StartPage... startPages) {
        if (startPages == null) {
            error("Start pages list is null");
        }
        if (startPages.length == 0) {
            error("Start pages list is empty");
        }
        this.startPages = startPages;
        return this;
    }

    public PacManGames_UIBuilder selectStartPage(int index) {
        // is checked after start page list has been specified
        this.selectedStartPageIndex = index;
        return this;
    }

    public PacManGames_UIBuilder dashboardEntries(DashboardID... ids) {
        if (ids == null) {
            error("Dashboard entry list is null");
        }
        if (ids.length == 0) {
            error("Dashboard entry list is empty");
        }
        this.dashboardIDs = ids;
        return this;
    }

    public PacManGames_UIBuilder stage(Stage stage, int width, int height) {
        if (stage == null) {
            error("Stage is null");
        }
        if (width <= 0) {
            error("Stage width (%d) must be a positive number".formatted(width));
        }
        if (height <= 0) {
            error("Stage height (%d) must be a positive number".formatted(height));
        }
        this.stage = stage;
        this.width = width;
        this.height = height;
        return this;
    }

    public void show() {
        var ui = new PacManGames_UI_Impl(configs);
        ui.buildUI(stage, width, height, dashboardIDs);
        models.forEach((variant, model) -> theGameController().registerGame(variant, model));
        theGameController().setEventsEnabled(true);
        for (StartPage startPage : startPages) ui.startPagesView().addStartPage(startPage);
        if (selectedStartPageIndex < 0 || selectedStartPageIndex >= startPages.length) {
            error("Selected start page index (%d) is out of range 0..%d".formatted(selectedStartPageIndex, startPages.length - 1));
        }
        ui.startPagesView().selectStartPage(selectedStartPageIndex);
        ui.startPagesView().currentStartPage()
            .map(StartPage::currentGameVariant)
            .ifPresent(theGameController()::selectGameVariant);
        theUI = ui;
        theUI.show();
    }
}
