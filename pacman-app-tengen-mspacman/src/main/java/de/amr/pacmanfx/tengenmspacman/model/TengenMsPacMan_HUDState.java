/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.core.model.HUDState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class TengenMsPacMan_HUDState extends HUDState {

    public final BooleanProperty levelNumberVisible = new SimpleBooleanProperty();
    public final BooleanProperty gameOptionsVisible = new SimpleBooleanProperty();

    public TengenMsPacMan_HUDState() {}

    public TengenMsPacMan_HUDState showGameOptions() {
        gameOptionsVisible.set(true);
        return this;
    }

    public TengenMsPacMan_HUDState hideGameOptions() {
        gameOptionsVisible.set(false);
        return this;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible.get();
    }

    public TengenMsPacMan_HUDState showLevelNumber() {
        levelNumberVisible.set(true);
        return this;
    }

    public TengenMsPacMan_HUDState hideLevelNumber() {
        levelNumberVisible.set(false);
        return this;
    }

    public boolean isLevelNumberVisible() {
        return levelNumberVisible.get();
    }
}