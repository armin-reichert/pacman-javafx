/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

public interface GameLevelEntity {

    default void init(GameLevel level) {}

    default void update(GameLevel level) {};
}
