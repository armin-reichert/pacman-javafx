/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.livescounter.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class LivesCounterData implements GameEntityComp {

    private int maxLives;

    private int numLives;

    public int numLives() {
        return numLives;
    }

    public void setNumLives(int numLives) {
        this.numLives = numLives;
    }

    public int maxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }

    @Override
    public void reset() {
        numLives = 0;
        maxLives = 5;
    }
}
