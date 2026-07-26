/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.component.Elroy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;

public class GhostFactory {

    public static Ghost createRedGhostShadow(String name, WorldMovementSystem worldMovementSystem) {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, name);

        ghost.setHuntingStrategy((GameContext gameContext, Float speed) -> {
            final GameLevel level = gameContext.assertLevel();

            worldMovementSystem.setSpeed(ghost, speed);

            final boolean chase = level.huntingRules().isChasing() || ghost.assertComponent(Elroy.class).enabled();

            final Vector2i targetTile = chase
                ? ghost.chasingTargetTileStrategy().apply(level)
                : level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());

            worldMovementSystem.tryMovingTowardsTargetTile(ghost, gameContext, targetTile);
        });

        ghost.setChasingTargetTileStrategy(level -> WorldMovementSystem.computeTile(level.entities().pac()));

        ghost.reset();
        return ghost;
    }

    public static Ghost createPinkGhostAmbusher(String name, WorldMovementSystem worldMovementSystem) {
        final Ghost ghost = new Ghost(GameModel.PINK_GHOST_SPEEDY, name);

        ghost.setChasingTargetTileStrategy(level -> {
            final Pac pac = level.entities().pac();
            return worldMovementSystem.tilesAheadWithOverflowBug(pac, 4);
        });

        ghost.reset();
        return ghost;
    }

    public static Ghost createCyanGhostBashful(String name, WorldMovementSystem worldMovementSystem) {
        final Ghost ghost = new Ghost(GameModel.CYAN_GHOST_BASHFUL, name);
        ghost.setChasingTargetTileStrategy(level -> {
            final Pac pac = level.entities().pac();
            final Vector2i blinkyTile = WorldMovementSystem.computeTile(level.ghost(RED_GHOST_SHADOW));
            return worldMovementSystem.tilesAheadWithOverflowBug(pac, 2).scaled(2).minus(blinkyTile);
        });
        ghost.reset();
        return ghost;
    }

    public static Ghost createOrangeGhostPokey(String name) {
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, name);

        ghost.setChasingTargetTileStrategy(level -> {
            final Vector2i pacTile = WorldMovementSystem.computeTile(level.entities().pac());
            final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
            final Vector2i ghostScatterTile = level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
            return ghostTile.euclideanDist(pacTile) < 8 ? ghostScatterTile : pacTile;
        });

        ghost.reset();
        return ghost;
    }
}
