/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.level;

import de.amr.pacmanfx.core.GameContext;

public interface GameEntity {

    default void init(GameContext gameContext) {}

    default void update(GameContext gameContext) {}
}
