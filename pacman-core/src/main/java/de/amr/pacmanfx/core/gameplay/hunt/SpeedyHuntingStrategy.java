/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay.hunt;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

public class SpeedyHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public SpeedyHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy<Ghost> worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);
        requireNonNull(worldMovementPolicy);

        final boolean chase = level.huntingTimer().inChasingPhase();
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile, worldMovementPolicy);
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        final Pac pac = level.entitySet().pac();
        return WorldNavigationSystem.tilesAheadWithOverflowBug(pac, 4);
    }
}
