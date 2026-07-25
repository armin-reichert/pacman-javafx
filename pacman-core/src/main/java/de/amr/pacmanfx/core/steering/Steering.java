/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.level.GameLevel;

public interface Steering {

    default void init() {}

    void steer(Actor actor, GameLevel level);
}