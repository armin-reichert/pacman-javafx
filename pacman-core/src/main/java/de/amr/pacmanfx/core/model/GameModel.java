/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.gameplay.ArcadeHouseGateKeeper;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelector;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * Base class of all Pac-Man game model classes.
 */
public abstract class GameModel {

    protected WorldMapSelector mapSelector;

    protected final ArcadeHouseGateKeeper gateKeeper;

    protected final IntegerProperty initialLifeCount = new SimpleIntegerProperty();

    // Constructor

    protected GameModel() {
        gateKeeper = new ArcadeHouseGateKeeper();
    }

    /* -------------------------------------------------------------------------
     * API
     * ---------------------------------------------------------------------- */

    public abstract void init();

    public int initialLifeCount() {
        return initialLifeCount.get();
    }

    public void setInitialLifeCount(int count) {
        initialLifeCount.set(count);
    }

    public ArcadeHouseGateKeeper gateKeeper() {
        return gateKeeper;
    }

    public WorldMapSelector mapSelector() {
        return mapSelector;
    }

    public abstract GameRules rules();
}
