/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import javafx.stage.Stage;

import java.util.*;

import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.ui.PacManGames_Env.theUI;

//TODO parameter validation
public class PacManGames_UIBuilder {

    private final Map<String, GameModel> models = new HashMap<>();
    private final Map<String, Class<? extends PacManGames_UIConfig>> configs = new HashMap<>();
    private StartPage[] startPages;
    private DashboardID[] dashboardIDs;
    private String initialVariant;
    private Stage stage;
    private int width = 800;
    private int height = 600;

    private PacManGames_UIBuilder() {
        PacManGames_Env.init();
    }

    public static PacManGames_UIBuilder buildUI() {
        return new PacManGames_UIBuilder();
    }

    public PacManGames_UIBuilder game(String variant, GameModel model, Class<? extends PacManGames_UIConfig> config) {
        models.put(variant, model);
        configs.put(variant, config);
        return this;
    }

    public PacManGames_UIBuilder selectGame(String variant) {
        initialVariant = variant;
        return this;
    }

    public PacManGames_UIBuilder startPages(StartPage... startPages) {
        this.startPages = startPages;
        return this;
    }

    public PacManGames_UIBuilder dashboardEntries(DashboardID... ids) {
        this.dashboardIDs = ids;
        return this;
    }

    public PacManGames_UIBuilder stage(Stage stage, int width, int height) {
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
        theGameController().selectGameVariant(initialVariant);
        for (StartPage startPage : startPages) ui.startPagesView().addStartPage(startPage);
        ui.startPagesView().selectStartPage(0);
        theUI = ui;
        theUI.show();
    }
}
