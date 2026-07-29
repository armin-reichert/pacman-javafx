/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

public class ShadowHuntingStrategy implements GhostHuntingStrategy {

    @Override
    public void hunt(GameContext gameContext, Ghost ghost, float speed) {
        final GameSystems sys = gameContext.systems();
        final GameLevel level = gameContext.assertLevel();

        final boolean chase = level.huntingRules().isChasing() || ghost.assertComponent(Elroy.class).enabled();
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        sys.navigator.setSpeed(ghost, speed);
        sys.navigator.tryMovingTowardsTargetTile(ghost, level, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        return WorldNavigationSystem.computeTile(level.entities().pac());
    }
}
