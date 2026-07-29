/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

/**
 * In Ms. Pac-Man, Blinky ("Shadow") and Pinky ("Speedy")  move randomly during the *first* scatter phase. Some say,
 * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
 * only the scatter target of Blinky and Pinky would have been affected. Who knows?
 */
public class ArcadeMsPacMan_ShadowHuntingStrategy extends ArcadeMsPacMan_RandomizedHuntingStrategy {

    public ArcadeMsPacMan_ShadowHuntingStrategy(WorldNavigationSystem navigator) {
        super(navigator);
    }

    @Override
    protected Vector2i computeChasingTargetTile(GameLevel level) {
        return WorldNavigationSystem.computeTile(level.entities().pac());
    }
}
