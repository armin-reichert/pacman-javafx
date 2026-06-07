/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.HUDState;

public class TengenMsPacMan_HUDState extends HUDState {

    private boolean levelNumberOn;
    private boolean gameOptionsOn;

    public TengenMsPacMan_HUDState() {}

    public TengenMsPacMan_HUDState gameOptionsOn() {
        gameOptionsOn = true;
        return this;
    }

    public TengenMsPacMan_HUDState gameOptionsOff() {
        gameOptionsOn = false;
        return this;
    }

    public boolean areGameOptionsOn() {
        return gameOptionsOn;
    }

    public TengenMsPacMan_HUDState levelNumberOn() {
        levelNumberOn = true;
        return this;
    }

    public TengenMsPacMan_HUDState levelNumberOff() {
        levelNumberOn = false;
        return this;
    }

    public boolean isLevelNumberOn() {
        return levelNumberOn;
    }
}