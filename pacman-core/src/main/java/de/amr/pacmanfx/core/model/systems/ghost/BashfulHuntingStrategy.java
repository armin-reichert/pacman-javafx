/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameSystems;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;
import static java.util.Objects.requireNonNull;

public class BashfulHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public BashfulHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed) {
        requireNonNull(level);
        requireNonNull(ghost);

        final Vector2i targetTile = level.huntingRules().isChasing()
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        final Pac pac = level.entities().pac();
        final Vector2i redGhostTile = WorldNavigationSystem.computeTile(level.ghost(RED_GHOST_SHADOW));
        return WorldNavigationSystem
            .tilesAheadWithOverflowBug(pac, 2)
            .scaled(2)
            .minus(redGhostTile);
    }
}
