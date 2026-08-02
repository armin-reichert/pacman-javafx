/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.ecs.systems.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.entities.ghost.ElroyComp;
import de.amr.pacmanfx.core.model.entities.ghost.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;

import static java.util.Objects.requireNonNull;

public class ShadowHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public ShadowHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);

        final boolean overrideChase = ghost.hasComponent(ElroyComp.class) && ghost.requireComponent(ElroyComp.class).enabled();
        final boolean chase = level.huntingRules().isChasing() || overrideChase;
        final Vector2i targetTile = chase ? computeChasingTargetTile(level) : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile, worldMovementPolicy);
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        return WorldNavigationSystem.computeTile(level.entities().pac());
    }
}
