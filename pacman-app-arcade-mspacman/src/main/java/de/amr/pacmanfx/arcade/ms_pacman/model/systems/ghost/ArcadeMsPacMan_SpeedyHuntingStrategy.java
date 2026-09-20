/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;

/**
 * In Ms. Pac-Man, Blinky ("Shadow") and Pinky ("Speedy")  move randomly during the *first* scatter phase. Some say,
 * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
 * only the scatter target of Blinky and Pinky would have been affected. Who knows?
 */
public class ArcadeMsPacMan_SpeedyHuntingStrategy extends ArcadeMsPacMan_RandomizedHuntingStrategy {

    public ArcadeMsPacMan_SpeedyHuntingStrategy(WorldNavigationSystem navigator) {
        super(navigator);
    }

    @Override
    protected Vector2i computeChasingTargetTile(GameLevel level) {
        final Pac pac = level.entities().pac();
        return WorldNavigationSystem.tilesAheadWithOverflowBug(pac, 4);
    }
}
