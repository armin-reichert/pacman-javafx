/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;

import static de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem.computeTile;
import static de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem.tilesAheadWithOverflowBug;
import static java.util.Objects.requireNonNull;

public class BashfulHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public BashfulHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy  worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);

        final Vector2i targetTile = level.huntingRules().isChasing()
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile, worldMovementPolicy);
    }

    // 1. Compute the position 2 tiles ahead of Pac-Man in the current direction. Take the overflow bug
    //    from the original Arcade game into account.
    // 2. Draw an arrow from Blinky's (red ghost) current tile to that position and double the arrow.
    //    The target tile for the "bashful" (cyan) ghost is the position where the arrow ends.
    private Vector2i computeChasingTargetTile(GameLevel level) {
        final Pac pac = level.entities().pac();
        final Vector2i pacAhead2 = tilesAheadWithOverflowBug(pac, 2);
        final Vector2i redGhostTile = computeTile(level.ghost(GhostPersonality.RED_GHOST_SHADOW));
        final Vector2i arrow = pacAhead2.minus(redGhostTile).scaled(2);
        return redGhostTile.plus(arrow);
    }
}
