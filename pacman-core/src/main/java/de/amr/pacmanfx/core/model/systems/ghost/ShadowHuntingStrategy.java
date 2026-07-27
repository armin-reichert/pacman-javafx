package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

public class ShadowHuntingStrategy implements GhostHuntingStrategy {

    @Override
    public void hunt(GameContext gameContext, Ghost ghost, float speed) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final GameLevel level = gameContext.assertLevel();

        worldMovementSystem.setSpeed(ghost, speed);
        final boolean chase = level.huntingRules().isChasing() || ghost.assertComponent(Elroy.class).enabled();
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(gameContext)
            : computeScatterTile(gameContext, ghost);
        worldMovementSystem.tryMovingTowardsTargetTile(ghost, gameContext, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        return WorldMovementSystem.computeTile(level.entities().pac());
    }

    private Vector2i computeScatterTile(GameContext gameContext, Ghost ghost) {
        final GameLevel level = gameContext.assertLevel();
        return level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
    }
}
