/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.HUDState;

public class TengenMsPacMan_HUDState extends HUDState {

    private boolean levelNumberVisible;
    private boolean gameOptionsVisible;

    public TengenMsPacMan_HUDState() {}

    public TengenMsPacMan_HUDState gameOptions(boolean visible) {
        gameOptionsVisible = visible;
        return this;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }

    public TengenMsPacMan_HUDState levelNumber(boolean visible) {
        levelNumberVisible = visible;
        return this;
    }

    public boolean levelNumberVisible() {
        return levelNumberVisible;
    }
}