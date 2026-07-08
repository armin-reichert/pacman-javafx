/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameVariantID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import javafx.stage.Stage;

public interface Game {

    PacManGamesMachine machine();

    GameUI createUI(GameUISettings settings, DashboardFactory dashboardFactory, Stage stage, int width, int height);

    void showUI(GameVariantID variantID);

    void setUI(GameUI ui);

    GameUI ui();

    GameVariantManager variantManager();

    CommonActions actions();

    GameContext context();

    GameExtensions extensions();

    void start();

    void pause();

    void terminate();
}
