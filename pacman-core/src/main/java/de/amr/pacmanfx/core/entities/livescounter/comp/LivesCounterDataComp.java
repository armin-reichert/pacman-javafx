/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.livescounter.comp;

import de.amr.pacmanfx.core.ecs.GameEntityComp;

public class LivesCounterDataComp implements GameEntityComp {

    private int maxLivesShown;

    private int numLivesShown;

    private int numLives;

    /** Number of lives shown in counter */
    public int numLivesShown() {
        return numLivesShown;
    }

    public void setNumLivesShown(int numLivesShown) {
        this.numLivesShown = numLivesShown;
    }

    public int maxLivesShown() {
        return maxLivesShown;
    }

    public void setMaxLivesShown(int maxLivesShown) {
        this.maxLivesShown = maxLivesShown;
    }

    /** Real number of lives in game session */
    public int numLives() {
        return numLives;
    }

    public void setNumLives(int numLives) {
        this.numLives = numLives;
    }

    @Override
    public void reset() {
        numLivesShown = 0;
        maxLivesShown = 5;
    }
}
