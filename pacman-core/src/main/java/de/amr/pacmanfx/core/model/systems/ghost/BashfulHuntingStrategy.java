package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;
import static java.util.Objects.requireNonNull;

public class BashfulHuntingStrategy implements GhostHuntingStrategy {

    @Override
    public void hunt(GameContext gameContext, Ghost ghost, float speed) {
        requireNonNull(gameContext);

        final GameSystems systems = gameContext.systems();
        final WorldMovementSystem navigator = systems.navigator;
        final GameLevel level = gameContext.assertLevel();

        final Vector2i targetTile = level.huntingRules().isChasing()
            ? computeChasingTargetTile(gameContext)
            : computeScatterTile(gameContext, ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, gameContext, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();
        final Vector2i redGhostTile = WorldMovementSystem.computeTile(level.ghost(RED_GHOST_SHADOW));
        return WorldMovementSystem
            .tilesAheadWithOverflowBug(pac, 2)
            .scaled(2)
            .minus(redGhostTile);
    }
}
