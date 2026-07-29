/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

import static java.util.Objects.requireNonNull;

public class PokeyHuntingStrategy implements GhostHuntingStrategy {

    @Override
    public void hunt(GameContext gameContext, Ghost ghost, float speed) {
        requireNonNull(gameContext);

        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();

        final Vector2i targetTile = level.huntingRules().isChasing()
            ? computeChasingTargetTile(level, ghost)
            : level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());

        sys.navigator.setSpeed(ghost, speed);
        sys.navigator.tryMovingTowardsTargetTile(ghost, level, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameLevel level, Ghost ghost) {
        final Vector2i pacTile = WorldNavigationSystem.computeTile(level.entities().pac());
        final Vector2i ghostTile = WorldNavigationSystem.computeTile(ghost);
        final Vector2i scatterTile = level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
        return ghostTile.euclideanDist(pacTile) < 8 ? scatterTile : pacTile;
    }
}
