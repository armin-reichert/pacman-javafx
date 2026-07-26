/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;

public class GhostFactory {

    public static Ghost createRedGhostShadow(String name) {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, name);

        ghost.setHuntingStrategy((GameLevel level, Float speed) -> {
            GameContext.SYSTEMS.worldMovementSystem.setSpeed(ghost, speed);
            final boolean chase = level.huntingRules().isChasing()
                || ghost.elroy().enabled();
            final Vector2i targetTile = chase
                ? ghost.chasingTargetTileStrategy().apply(level)
                : level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
            GameContext.SYSTEMS.worldMovementSystem.tryMovingTowardsTargetTile(ghost, level, targetTile);
        });

        ghost.setChasingTargetTileStrategy(level -> level.entities().pac().tile());

        ghost.reset();
        return ghost;
    }

    public static Ghost createPinkGhostAmbusher(String name) {
        final Ghost ghost = new Ghost(GameModel.PINK_GHOST_SPEEDY, name);

        ghost.setChasingTargetTileStrategy(level -> {
            final Pac pac = level.entities().pac();
            return GameContext.SYSTEMS.worldMovementSystem.tilesAheadWithOverflowBug(pac, 4);
        });

        ghost.reset();
        return ghost;
    }

    public static Ghost createCyanGhostBashful(String name) {
        final Ghost ghost = new Ghost(GameModel.CYAN_GHOST_BASHFUL, name);

        ghost.setChasingTargetTileStrategy(level -> {
            final Pac pac = level.entities().pac();
            return GameContext.SYSTEMS.worldMovementSystem.tilesAheadWithOverflowBug(pac, 2)
                .scaled(2)
                .minus(level.ghost(RED_GHOST_SHADOW).tile());
        });
        ghost.reset();
        return ghost;
    }

    public static Ghost createOrangeGhostPokey(String name) {
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, name);

        ghost.setChasingTargetTileStrategy(level ->
            ghost.tile().euclideanDist(level.entities().pac().tile()) < 8
            ? level.worldMap().terrainLayer().ghostScatterTile(ghost.personality())
            : level.entities().pac().tile()
        );

        ghost.reset();
        return ghost;
    }
}
