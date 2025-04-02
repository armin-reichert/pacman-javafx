/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.ui._3d.GameView3D;

public class Globals {
    public static GameUI THE_UI;

    public static void createUIWithout3DSupport() {
        THE_UI = new PacManGamesUI();
    }

    public static void createUIWith3DSupport() {
        THE_UI = new PacManGamesUI() {
            {
                assets().addAssets3D();
            }

            @Override
            protected void createGameView() {
                gameView = new GameView3D(this);
            }
        };
    }
}