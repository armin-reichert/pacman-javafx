/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.HUDState;

public class TengenMsPacMan_HeadsUpDisplay extends HUDState {

    private boolean levelNumberVisible;
    private boolean gameOptionsVisible;

    public TengenMsPacMan_HeadsUpDisplay() {}

    public TengenMsPacMan_HeadsUpDisplay gameOptions(boolean visible) {
        gameOptionsVisible = visible;
        return this;
    }

    @Override
    public TengenMsPacMan_HeadsUpDisplay all(boolean visible) {
        super.all(visible);
        return levelNumber(visible).gameOptions(visible);
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }

    public TengenMsPacMan_HeadsUpDisplay levelNumber(boolean visible) {
        levelNumberVisible = visible;
        return this;
    }

    public boolean levelNumberVisible() {
        return levelNumberVisible;
    }
}