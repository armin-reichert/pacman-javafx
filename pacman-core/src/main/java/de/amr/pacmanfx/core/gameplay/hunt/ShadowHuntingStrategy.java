/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay.hunt;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.level.GameLevel;

import static java.util.Objects.requireNonNull;

public class ShadowHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public ShadowHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, MovementSystem motor, float speed, WorldMovementPolicy worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);

        final boolean overrideChase = ghost.hasComp(ElroyComp.class) && ghost.reqComp(ElroyComp.class).enabled();
        final boolean chase = level.huntingTimerStrategy().isChasing() || overrideChase;
        final Vector2i targetTile = chase ? computeChasingTargetTile(level) : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(motor, ghost, level, targetTile, worldMovementPolicy);
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        return level.entities().pac().pos().tile();
    }
}
