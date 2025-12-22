/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.BaseHUD;

public class TengenMsPacMan_HUD extends BaseHUD {

    private boolean levelNumberVisible;
    private boolean gameOptionsVisible;

    public TengenMsPacMan_HUD gameOptions(boolean visible) {
        gameOptionsVisible = visible;
        return this;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }

    public TengenMsPacMan_HUD levelNumber(boolean visible) {
        levelNumberVisible = visible;
        return this;
    }

    public boolean levelNumberVisible() {
        return levelNumberVisible;
    }
}