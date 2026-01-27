/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.HeadsUpDisplay;

public class TengenMsPacMan_HeadsUpDisplay extends HeadsUpDisplay {

    private boolean levelNumberVisible;
    private boolean gameOptionsVisible;

    public TengenMsPacMan_HeadsUpDisplay() {
        super(CoinMechanism.MISSING);
    }

    public TengenMsPacMan_HeadsUpDisplay gameOptions(boolean visible) {
        gameOptionsVisible = visible;
        return this;
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