/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_SpeedyHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public ArcadeMsPacMan_SpeedyHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed) {
        requireNonNull(level);
        requireNonNull(ghost);

        final boolean chase = level.huntingRules().isChasing();
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile);
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        final Pac pac = level.entities().pac();
        return WorldNavigationSystem.tilesAheadWithOverflowBug(pac, 4);
    }
}
