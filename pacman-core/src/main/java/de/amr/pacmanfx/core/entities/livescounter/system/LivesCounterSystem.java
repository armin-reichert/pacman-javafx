/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.livescounter.system;


import de.amr.pacmanfx.core.entities.LivesCounter;

public class LivesCounterSystem {

    public static void setNumLives(LivesCounter livesCounter, int numLives) {
        livesCounter.data().setNumLives(numLives);
    }

    public static void addLife(LivesCounter livesCounter) {
        setNumLives(livesCounter, livesCounter.data().numLives() + 1);
    }

    public static void addLives(LivesCounter livesCounter, int lives) {
        setNumLives(livesCounter, livesCounter.data().numLives() + lives);
    }

    public static void subtractLife(LivesCounter livesCounter) {
        final int numLives = livesCounter.data().numLives();
        if (numLives == 0) {
            throw new IllegalStateException("Cannot subtract life, no lives remaining!");
        }
        setNumLives(livesCounter, numLives - 1);
    }
}
