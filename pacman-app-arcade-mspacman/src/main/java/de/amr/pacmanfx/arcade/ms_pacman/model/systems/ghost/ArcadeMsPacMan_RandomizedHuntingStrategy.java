/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model.systems.ghost;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.comp.ghost.ElroyComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.ghost.GhostHuntingStrategy;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

/**
 * In Ms. Pac-Man, Blinky ("Shadow") and Pinky ("Speedy")  move randomly during the *first* scatter phase. Some say,
 * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
 * only the scatter target of Blinky and Pinky would have been affected. Who knows?
 */
public abstract class ArcadeMsPacMan_RandomizedHuntingStrategy implements GhostHuntingStrategy {

    protected final WorldNavigationSystem navigator;

    public ArcadeMsPacMan_RandomizedHuntingStrategy(WorldNavigationSystem navigator) {
        this.navigator = requireNonNull(navigator);
    }

    protected abstract Vector2i computeChasingTargetTile(GameLevel level);

    @Override
    public void hunt(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy worldMovementPolicy) {
        requireNonNull(level);
        requireNonNull(ghost);

        if (level.huntingRules().phaseIndex() == 0) { // first scatter phase
            moveRandomlyThroughWorld(level, ghost, speed, worldMovementPolicy);
        }
        else {
            normalHunt(level, ghost, speed, worldMovementPolicy);
        }
    }

    protected void normalHunt(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy worldMovementPolicy) {
        final boolean chaseOverride = ghost.hasComponent(ElroyComp.class) && ghost.requireComponent(ElroyComp.class).enabled();
        final boolean chase = level.huntingRules().isChasing() || chaseOverride;
        final Vector2i targetTile = chase
            ? computeChasingTargetTile(level)
            : computeScatterTile(level.worldMap(), ghost);

        navigator.setSpeed(ghost, speed);
        navigator.tryMovingTowardsTargetTile(ghost, level, targetTile, worldMovementPolicy);
    }
    
    protected void moveRandomlyThroughWorld(GameLevel level, Ghost ghost, float speed, WorldMovementPolicy worldMovementPolicy) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        final Vector2i tile = WorldNavigationSystem.computeTile(ghost);

        final boolean teleporting = terrain.isTileInPortalSpace(tile);
        if (teleporting) {
            navigator.setSpeed(ghost, speed);
            navigator.tryMovingOrTeleporting(ghost, level, worldMovementPolicy);
            return;
        }

        final boolean changeWishDirection = !ghost.worldNavigation().info.moved
          || (ghost.worldNavigation().isNewTileEntered() && terrain.isIntersection(tile));
        if (changeWishDirection) {
            selectRandomWishDir(ghost, level, worldMovementPolicy);
        }
        navigator.setSpeed(ghost, speed);
        navigator.tryMovingOrTeleporting(ghost, level, worldMovementPolicy);
    }

    private void selectRandomWishDir(Ghost ghost, GameLevel level, WorldMovementPolicy worldMovementPolicy) {
        final Vector2i ghostTile = WorldNavigationSystem.computeTile(ghost);

        for (final Direction dir : Direction.shuffled()) {
            final Vector2i neighbor = ghostTile.plus(dir.vector());
            final boolean acceptable = dir != ghost.worldNavigation().moveDir().opposite()
                && worldMovementPolicy.canAccessTile(level, ghost, neighbor);
            if (acceptable) {
                navigator.setWishDir(ghost, dir);
                Logger.info("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
        }
    }
}
