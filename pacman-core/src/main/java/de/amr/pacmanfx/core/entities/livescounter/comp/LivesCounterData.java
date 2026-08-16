/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.livescounter.comp;


import de.amr.pacmanfx.core.ecs.EntityComponent;

public class LivesCounterData implements EntityComponent {

    private int numLives;

    public int numLives() {
        return numLives;
    }

    public void setNumLives(int numLives) {
        this.numLives = numLives;
    }

    @Override
    public void reset() {
        numLives = 0;
    }
}
