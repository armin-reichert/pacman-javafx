/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelector;

/**
 * Base class of all Pac-Man game model classes.
 */
public abstract class GameModel {

    protected WorldMapSelector mapSelector;

    private int initialLifeCount = 3;

    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
    }

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    public abstract GameRules rules();
}
