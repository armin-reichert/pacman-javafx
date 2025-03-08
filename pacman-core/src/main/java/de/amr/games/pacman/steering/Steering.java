/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.steering;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.actors.Creature;

/**
 * @author Armin Reichert
 */
public interface Steering {

    default void init() {}

    void steer(Creature creature, GameLevel level);
}