/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

public interface GameLevelAware {

    void init(GameLevel level);

    void update(GameLevel level);
}
