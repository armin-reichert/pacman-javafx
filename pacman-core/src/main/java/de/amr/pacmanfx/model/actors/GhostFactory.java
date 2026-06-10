/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.Globals_Core;
import de.amr.pacmanfx.model.level.GameLevel;

import static de.amr.pacmanfx.core.Globals_Core.RED_GHOST_SHADOW;

public class GhostFactory {

    public static Ghost createRedGhostShadow(String name) {
        final Ghost ghost = new Ghost(Globals_Core.RED_GHOST_SHADOW, name);

        ghost.setHuntingStrategy((GameLevel level, Float speed) -> {
            ghost.setSpeed(speed);
            final boolean chase = level.huntingTimer().isChasing()
                || ghost.elroy().enabled();
            final Vector2i targetTile = chase
                ? ghost.chasingTargetTileStrategy().apply(level)
                : level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
            ghost.tryMovingTowardsTargetTile(level, targetTile);
        });

        ghost.setChasingTargetTileStrategy(level -> level.entities().pac().computeTile());

        ghost.reset();
        return ghost;
    }

    public static Ghost createPinkGhostAmbusher(String name) {
        final Ghost ghost = new Ghost(Globals_Core.PINK_GHOST_SPEEDY, name);

        ghost.setChasingTargetTileStrategy(level -> level.entities().pac().tilesAheadWithOverflowBug(4));

        ghost.reset();
        return ghost;
    }

    public static Ghost createCyanGhostBashful(String name) {
        final Ghost ghost = new Ghost(Globals_Core.CYAN_GHOST_BASHFUL, name);

        ghost.setChasingTargetTileStrategy(level ->
            level.entities().pac().tilesAheadWithOverflowBug(2)
                .scaled(2)
                .minus(level.ghost(RED_GHOST_SHADOW).computeTile()));

        ghost.reset();
        return ghost;
    }

    public static Ghost createOrangeGhostPokey(String name) {
        final Ghost ghost = new Ghost(Globals_Core.ORANGE_GHOST_POKEY, name);

        ghost.setChasingTargetTileStrategy(level ->
            ghost.computeTile().euclideanDist(level.entities().pac().computeTile()) < 8
            ? level.worldMap().terrainLayer().ghostScatterTile(ghost.personality())
            : level.entities().pac().computeTile()
        );

        ghost.reset();
        return ghost;
    }
}
