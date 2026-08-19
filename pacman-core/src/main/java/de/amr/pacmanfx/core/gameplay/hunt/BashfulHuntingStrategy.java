/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay.hunt;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;

import static de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem.tilesAheadWithOverflowBug;
import static java.util.Objects.requireNonNull;

public class BashfulHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public BashfulHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, MovementSystem motor, float speed, WorldMovementPolicy worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);

        final Vector2i targetTile = level.huntingTimerStrategy().isChasing()
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setMoveDirSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(motor, ghost, level, targetTile, worldMovementPolicy);
    }

    // 1. Compute the position 2 tiles ahead of Pac-Man in the current direction. Take the overflow bug
    //    from the original Arcade game into account.
    // 2. Draw an arrow from Blinky's (red ghost) current tile to that position and double the arrow.
    //    The target tile for the "bashful" (cyan) ghost is the position where the arrow ends.
    private Vector2i computeChasingTargetTile(GameLevel level) {
        final Pac pac = level.entities().pac();
        final Ghost redGhost = level.entities().ghost(GhostPersonality.RED_GHOST_SHADOW);
        final Vector2i pacAhead2 = tilesAheadWithOverflowBug(pac, 2);
        final Vector2i redGhostTile = redGhost.pos().tile();
        final Vector2i arrow = pacAhead2.minus(redGhostTile).scaled(2);
        return redGhostTile.plus(arrow);
    }
}
