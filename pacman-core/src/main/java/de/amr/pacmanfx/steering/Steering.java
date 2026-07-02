/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.level.GameLevel;

public interface Steering {

    default void init() {}

    void steer(MovingActor actor, GameLevel level);
}