/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.model.actors.Creature;

/**
 * @author Armin Reichert
 */
public interface Steering {

    default void init() {}

    void steer(Creature creature);
}