/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ui.entities.hud.livescounter;

import de.amr.basics.ecs.Resettable;

public class LivesCounterDataComp implements Resettable {

    private int maxLivesShown;

    private int numLivesShown;

    private int numLives;

    public LivesCounterDataComp() {}

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
