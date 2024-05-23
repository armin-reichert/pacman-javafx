/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.steering;

import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.world.World;

/**
 * @author Armin Reichert
 */
public interface Steering {

    default void init() {}

    void steer(Creature creature, World world);
}