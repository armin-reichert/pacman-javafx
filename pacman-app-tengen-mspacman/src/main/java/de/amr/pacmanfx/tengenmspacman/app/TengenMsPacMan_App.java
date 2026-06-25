/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.app;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.tengenmspacman.*;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig.TengenMsPacMan_DashboardID;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GameBuilder;
import de.amr.pacmanfx.ui.game.PacManGamesMachine;
import de.amr.pacmanfx.ui.views.dashboard.DashboardID;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

import static de.amr.pacmanfx.core.GameVariantID.TENGEN_MS_PACMAN;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacManConfig.NES_SCREEN_ASPECT_RATIO;

public class TengenMsPacMan_App extends Application {

    private static final float ASPECT_RATIO    = NES_SCREEN_ASPECT_RATIO; // 32:30
    private static final float HEIGHT_FRACTION = 0.8f; // Use 80% of available height

    private final PacManGamesMachine machine = new PacManGamesMachine();
    private Game game;

    @Override
    public void init() {
        machine.loadCartridge(TengenMsPacMan_Cartridge.CARTRIDGE);
    }

    @Override
    public void start(Stage stage) {
        final Vector2i sceneSize = Ufx.computeScreenSectionSize(ASPECT_RATIO, HEIGHT_FRACTION);

        game = new GameBuilder(machine, sceneSize.x(), sceneSize.y())
            .startPage(TengenMsPacMan_StartPage::new)
            .build(stage);

        //TODO
        //playView.dashboard().addSection(TengenMsPacMan_DashboardID.JOYPAD, new DashboardSectionJoypad(playView.dashboard()));

        game.extensions().add(TengenMsPacMan_GameExtension.UI_SETTINGS, new TengenMsPacMan_UISettings());
        game.extensions().add(TengenMsPacMan_GameExtension.ACTIONS, new TengenMsPacMan_Actions(game));

        game.showUI(TENGEN_MS_PACMAN);
    }

    @Override
    public void stop() {
        game.terminate();
    }
}