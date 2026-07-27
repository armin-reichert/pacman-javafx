package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostFactory;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.model.world.WorldMap.halfTileRightOf;

public class ArcadeMsPacMan_ActorFactory {

    public static Pac createMsPacMan() {
        return new Pac("Ms. Pac-Man");
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    public static Ghost createGhost(GameContext gameContext, byte personality) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> modifyShadowBehavior(
                GhostFactory.createRedGhostShadow("Blinky", worldMovementSystem));

            case GameModel.PINK_GHOST_SPEEDY  -> modifyAmbushBehavior(
                GhostFactory.createPinkGhostAmbusher("Pinky", worldMovementSystem));

            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky", worldMovementSystem);

            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Sue");

            default -> throw new IllegalArgumentException("Illegal ghost personality: %d".formatted(personality));
        };
    }

    public static Ghost createGhost(GameContext gameContext, byte personality, TerrainLayer terrain, House house, String startTileProperty) {
        final Ghost ghost = createGhost(gameContext, personality);
        ghost.setHouse(house);
        ghost.setStartPosition(halfTileRightOf(terrain.getTileProperty(startTileProperty)));
        return ghost;
    }

    private static Ghost modifyShadowBehavior(Ghost redGhost) {
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
        return redGhost;
    }

    private static Ghost modifyAmbushBehavior(Ghost pinkGhost) {
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
        return pinkGhost;
    }

    private static void selectRandomWishDir(Ghost ghost, GameContext gameContext) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final WorldMovementPolicy policy = ghost.assertComponent(WorldMovementPolicy.class);
        final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
        
        for (final Direction dir : Direction.shuffled()) {
            final Vector2i neighbor = ghostTile.plus(dir.vector());
            final boolean acceptable = dir != ghost.worldMovement().moveDir().opposite()
                && policy.canAccessTile(gameContext, ghost, neighbor);
            if (acceptable) {
                worldMovementSystem.setWishDir(ghost, dir);
                Logger.debug("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
        }
    }
}
