/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib;

import de.amr.games.pacman.model.actors.Creature;

/**
 * @author Armin Reichert
 */
public abstract class Steering {

    public void init() {}

    public abstract void steer(Creature creature);
}