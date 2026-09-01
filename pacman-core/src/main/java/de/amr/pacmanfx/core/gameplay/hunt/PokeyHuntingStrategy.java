/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay.hunt;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

public class PokeyHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public PokeyHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy<Ghost> worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);
        requireNonNull(worldMovementPolicy);

        final Vector2i targetTile = level.huntingTimer().inChasingPhase()
            ? computeChasingTargetTile(level, ghost)
            : level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());

        navigator.setMoveDirSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile, worldMovementPolicy);
    }

    private Vector2i computeChasingTargetTile(GameLevel level, Ghost ghost) {
        final Vector2i pacTile = level.entities().pac().pos().tile();
        final Vector2i ghostTile = ghost.pos().tile();
        final Vector2i scatterTile = level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
        return ghostTile.euclideanDist(pacTile) < 8 ? scatterTile : pacTile;
    }
}
