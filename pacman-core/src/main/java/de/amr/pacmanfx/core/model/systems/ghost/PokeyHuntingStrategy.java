/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

import static java.util.Objects.requireNonNull;

public class PokeyHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public PokeyHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed) {
        requireNonNull(level);
        requireNonNull(ghost);

        final Vector2i targetTile = level.huntingRules().isChasing()
            ? computeChasingTargetTile(level, ghost)
            : level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameLevel level, Ghost ghost) {
        final Vector2i pacTile = WorldNavigationSystem.computeTile(level.entities().pac());
        final Vector2i ghostTile = WorldNavigationSystem.computeTile(ghost);
        final Vector2i scatterTile = level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
        return ghostTile.euclideanDist(pacTile) < 8 ? scatterTile : pacTile;
    }
}
