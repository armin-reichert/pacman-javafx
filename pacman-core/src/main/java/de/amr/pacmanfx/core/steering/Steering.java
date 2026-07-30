/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.steering;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;

public interface Steering<A extends Actor> {

    default void init() {}

    void steer(A actor, GameContext gameContext);
}