/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

public class Globals {
    public static GameUI THE_UI;

    public static void createUIAndSupport3D(boolean support3D) {
        THE_UI = new PacManGamesUI();
        if (support3D) {
            THE_UI.assets().addAssets3D();
        }
    }
}