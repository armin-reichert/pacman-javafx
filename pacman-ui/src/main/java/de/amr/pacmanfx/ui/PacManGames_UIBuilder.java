package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.dashboard.DashboardID;
import de.amr.pacmanfx.ui.layout.StartPage;
import javafx.stage.Stage;

import java.util.*;

import static de.amr.pacmanfx.Globals.theGameController;

public class PacManGames_UIBuilder {

    private final Map<String, GameModel> models = new HashMap<>();
    private final Map<String, Class<? extends PacManGames_UIConfig>> configs = new HashMap<>();
    private final List<StartPage> startPageList = new ArrayList<>();
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

    public PacManGames_UIBuilder game(String variant, GameModel model) {
        models.put(variant, model);
        return this;
    }

    public PacManGames_UIBuilder uiConfig(String variant, Class<? extends PacManGames_UIConfig> config) {
        configs.put(variant, config);
        return this;
    }

    public PacManGames_UIBuilder selectGame(String variant) {
        initialVariant = variant;
        return this;
    }

    public PacManGames_UIBuilder startPage(StartPage startPage) {
        startPageList.add(startPage);
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
        PacManGames_Env.theUI = new PacManGames_UI_Impl(configs);
        PacManGames_Env.theUI.buildUI(stage, width, height, dashboardIDs);
        startPageList.forEach(PacManGames_Env.theUI.startPagesView()::addStartPage);
        PacManGames_Env.theUI.startPagesView().selectStartPage(0);
        models.forEach((variant, model) -> {
            theGameController().registerGame(variant, model);
            model.init();

        });
        theGameController().setEventsEnabled(true);
        theGameController().selectGameVariant(initialVariant);
        PacManGames_Env.theUI.show();
    }
}
