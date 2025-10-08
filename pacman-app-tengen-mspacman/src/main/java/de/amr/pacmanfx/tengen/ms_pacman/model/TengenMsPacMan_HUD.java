/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.BaseHUD;

public class TengenMsPacMan_HUD extends BaseHUD {

    private boolean levelNumberVisible;
    private boolean gameOptionsVisible;

    public void showGameOptions(boolean visible) {
        gameOptionsVisible = visible;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }

    public void showLevelNumber(boolean visible) {
        levelNumberVisible = visible;
    }

    public boolean levelNumberVisible() {
        return levelNumberVisible;
    }
}