/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.livescounter.system;


import de.amr.pacmanfx.core.entities.LivesCounter;

public class LivesCounterSystem {

    public static void setNumLives(LivesCounter livesCounter, int numLives) {
        livesCounter.data().setNumLives(numLives);
    }

    public static void addLives(LivesCounter livesCounter, int lives) {
        setNumLives(livesCounter, livesCounter.data().numLives() + lives);
    }
}
