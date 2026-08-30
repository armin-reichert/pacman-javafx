/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.livescounter.system;

import de.amr.pacmanfx.core.entities.LivesCounter;

import static java.util.Objects.requireNonNull;

public class LivesCounterSystem {

    public void setNumLives(LivesCounter livesCounter, int n) {
        requireNonNull(livesCounter);
        final var data = livesCounter.data();
        data.setNumLives(n);
    }

    public void addLives(LivesCounter livesCounter, int n) {
        requireNonNull(livesCounter);
        final var data = livesCounter.data();
        data.setNumLives(data.numLives() + 1);
    }

    public void setMaxLivesShown(LivesCounter livesCounter, int n) {
        requireNonNull(livesCounter);
        final var data = livesCounter.data();
        data.setMaxLivesShown(n);
    }
}
