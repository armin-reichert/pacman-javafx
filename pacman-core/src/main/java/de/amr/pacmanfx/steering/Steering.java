/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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