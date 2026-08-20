/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.level.GameLevel;

public interface Steering<E extends GameEntity> {

    default void init() {}

    void steer(E gameEntity, GameLevel level);
}