/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_ShadowHuntingStrategy implements GhostHuntingStrategy {

    private final WorldNavigationSystem navigator;

    public ArcadeMsPacMan_ShadowHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed) {
        requireNonNull(level);
        requireNonNull(ghost);
        if (level.huntingRules().phaseIndex() == 0) {
            moveRandomlyThroughWorld(level, ghost, speed);
        }
        else {
            normalHunt(level, ghost, speed);
        }
    }

    private Vector2i computeChasingTargetTile(GameLevel level) {
        return WorldNavigationSystem.computeTile(level.entities().pac());
    }

    private void normalHunt(GameLevel level, Ghost ghost, float speed) {
        final boolean chase = level.huntingRules().isChasing() || ghost.assertComponent(Elroy.class).enabled();
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile);
    }
    
    private void moveRandomlyThroughWorld(GameLevel level, Ghost ghost, float speed) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i tile = WorldNavigationSystem.computeTile(ghost);

        final boolean teleporting = terrain.isTileInPortalSpace(tile);
        if (teleporting) {
            navigator.setSpeed(ghost, speed);
            navigator.tryMovingOrTeleporting(ghost, level);
            return;
        }

        final boolean changeWishDirection = !ghost.worldNavigation().info.moved
          || (ghost.worldNavigation().isNewTileEntered() && terrain.isIntersection(tile));
        if (changeWishDirection) {
            selectRandomWishDir(ghost, level);
        }
        navigator.setSpeed(ghost, speed);
        navigator.tryMovingOrTeleporting(ghost, level);
    }

    private void selectRandomWishDir(Ghost ghost, GameLevel level) {
        final WorldMovementPolicy policy = ghost.assertComponent(WorldMovementPolicy.class);
        final Vector2i ghostTile = WorldNavigationSystem.computeTile(ghost);

        for (final Direction dir : Direction.shuffled()) {
            final Vector2i neighbor = ghostTile.plus(dir.vector());
            final boolean acceptable = dir != ghost.worldNavigation().moveDir().opposite()
                && policy.canAccessTile(level, ghost, neighbor);
            if (acceptable) {
                navigator.setWishDir(ghost, dir);
                Logger.info("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
        }
    }
}
