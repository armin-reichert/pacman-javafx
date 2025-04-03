/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

public class Globals {
    public static GameUI THE_UI;

    public static void createUIWithout3DSupport() {
        THE_UI = new PacManGamesUI();
    }

    public static void createUIWith3DSupport() {
        THE_UI = new PacManGamesUI();
        THE_UI.assets().addAssets3D();
    }
}