/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.steering;

import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.MovingActor;

/**
 * @author Armin Reichert
 */
public interface Steering {

    default void init() {}

    void steer(MovingActor movingActor, GameLevel level);
}