package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import org.tinylog.Logger;

public class ArcadeMsPacMan_ActorFactory extends ArcadePacMan_ActorFactory {

    public Pac createMsPacMan() {
        return new Pac("Ms. Pac-Man");
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    public Ghost createRedGhost() {
        final Ghost ghost = super.createRedGhost();
        //TODO modify behavior
        return ghost;
    }

    public Ghost createPinkGhost() {
        final Ghost ghost = super.createPinkGhost();
        //TODO modify behavior
        return ghost;
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = super.createOrangeGhost();
        ghost.setName("Sue");
        return ghost;
    }

    private static Ghost modifyShadowBehavior(Ghost redGhost) {

        //TODO create strategy class
/*
        redGhost.setHuntingStrategy((GameContext gameContext, Float speed) -> {
            final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
            final GameLevel level = gameContext.assertLevel();
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            
            final Vector2i redGhostTile = WorldMovementSystem.computeTile(redGhost);
            final boolean teleporting = terrain.isTileInPortalSpace(redGhostTile);

            if (teleporting) {
                worldMovementSystem.setSpeed(redGhost, speed);
                worldMovementSystem.tryMovingOrTeleporting(redGhost, gameContext);
                return;
            }

            final boolean takeRandomDir = level.huntingRules().phaseIndex() == 0
                && redGhost.worldMovement().isNewTileEntered()
                && terrain.isIntersection(redGhostTile);

            if (takeRandomDir) {
                selectRandomWishDir(redGhost, gameContext);
                worldMovementSystem.setSpeed(redGhost, speed);
                worldMovementSystem.tryMovingOrTeleporting(redGhost, gameContext);
            }
            else {
                // Normal behavior of red ghost
                final boolean chase = level.huntingRules().isChasing() || redGhost.assertComponent(Elroy.class).enabled();
                final Vector2i targetTile = chase
                    ? redGhost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(redGhost.personality());
                worldMovementSystem.setSpeed(redGhost, speed);
                worldMovementSystem.tryMovingTowardsTargetTile(redGhost, gameContext, targetTile);
            }
        });

 */
        return redGhost;
    }

    private static Ghost modifyAmbushBehavior(Ghost pinkGhost) {
        //TODO create strategy class

        /*
        pinkGhost.setHuntingStrategy((GameContext gameContext, Float speed) -> {
            final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
            final GameLevel level = gameContext.assertLevel();
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final Vector2i pinkGhostTile = WorldMovementSystem.computeTile(pinkGhost);
            final boolean teleporting = terrain.isTileInPortalSpace(pinkGhostTile);

            if (teleporting) {
                worldMovementSystem.setSpeed(pinkGhost, speed);
                worldMovementSystem.tryMovingOrTeleporting(pinkGhost, gameContext);
                return;
            }

            final boolean takeRandomDir = level.huntingRules().phaseIndex() == 0
                && pinkGhost.worldMovement().isNewTileEntered()
                && terrain.isIntersection(pinkGhostTile);

            if (takeRandomDir) {
                selectRandomWishDir(pinkGhost, gameContext);
                worldMovementSystem.setSpeed(pinkGhost, speed);
                worldMovementSystem.tryMovingOrTeleporting(pinkGhost, gameContext);
            }
            else {
                final boolean chase = level.huntingRules().isChasing();
                final Vector2i targetTile = chase
                    ? pinkGhost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(pinkGhost.personality());
                worldMovementSystem.setSpeed(pinkGhost, speed);
                worldMovementSystem.tryMovingTowardsTargetTile(pinkGhost, gameContext, targetTile);
            }
        });

         */
        return pinkGhost;
    }

    private static void selectRandomWishDir(Ghost ghost, GameContext gameContext) {
        final GameSystems sys = gameContext.systems();
        final WorldMovementPolicy policy = ghost.assertComponent(WorldMovementPolicy.class);
        final Vector2i ghostTile = WorldNavigationSystem.computeTile(ghost);
        final GameLevel level = gameContext.assertLevel();

        for (final Direction dir : Direction.shuffled()) {
            final Vector2i neighbor = ghostTile.plus(dir.vector());
            final boolean acceptable = dir != ghost.worldNavigation().moveDir().opposite()
                && policy.canAccessTile(level, ghost, neighbor);
            if (acceptable) {
                sys.navigator().setWishDir(ghost, dir);
                Logger.debug("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
        }
    }
}
