/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.lives;

public interface PacManLives {

    int initialCount();

    void setInitialCount(int numLives);

    void setCount(int numLives);

    int count();

    void add(int numLives);
}
