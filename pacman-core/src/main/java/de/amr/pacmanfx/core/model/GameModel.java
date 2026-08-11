/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelector;

/**
 * Base class of all Pac-Man game model classes.
 */
public abstract class GameModel {

    protected WorldMapSelector mapSelector;

    protected final ArcadeHouseGateKeeper gateKeeper;

    protected int initialLifeCount;

    protected GameModel() {
        gateKeeper = new ArcadeHouseGateKeeper();
    }

    public abstract void init();

    public int initialLifeCount() {
        return initialLifeCount;
    }

    public void setInitialLifeCount(int count) {
        initialLifeCount = count;
    }

    public ArcadeHouseGateKeeper gateKeeper() {
        return gateKeeper;
    }

    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    public abstract GameRules rules();
}
