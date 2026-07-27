package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

public class SpeedyHuntingStrategy implements GhostHuntingStrategy{

    @Override
    public void hunt(GameContext gameContext, Ghost ghost, float speed) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().navigator;
        final GameLevel level = gameContext.assertLevel();

        final boolean chase = level.huntingRules().isChasing();
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(gameContext)
            : computeScatterTile(gameContext, ghost);

        worldMovementSystem.setSpeed(ghost, speed);
        worldMovementSystem.tryMovingTowardsTargetTile(ghost, gameContext, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        return WorldMovementSystem.tilesAheadWithOverflowBug(pac, 4);
    }

    private Vector2i computeScatterTile(GameContext gameContext, Ghost ghost) {
        final GameLevel level = gameContext.assertLevel();
        return level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
    }

}
