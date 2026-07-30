/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.pacmanfx.core.model.actors.GameEntity;
import de.amr.pacmanfx.core.model.level.GameLevel;

public interface Steering<A extends GameEntity> {

    default void init() {}

    void steer(A actor, GameLevel level);
}