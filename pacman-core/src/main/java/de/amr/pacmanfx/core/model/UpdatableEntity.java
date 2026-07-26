/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.GameContext;

public interface UpdatableEntity {

    default void init(GameContext gameContext) {}

    default void update(GameContext gameContext) {}
}
