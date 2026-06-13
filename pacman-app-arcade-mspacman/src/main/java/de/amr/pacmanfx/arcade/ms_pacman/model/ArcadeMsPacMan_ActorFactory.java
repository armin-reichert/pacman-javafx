package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostFactory;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.House;
import de.amr.pacmanfx.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.model.world.WorldMap.halfTileRightOf;

public class ArcadeMsPacMan_ActorFactory {

    public static Pac createMsPacMan() {
        return new Pac("Ms. Pac-Man");
    }

    /**
     * In Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     */
    public static Ghost createGhost(byte personality) {
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> modifyShadowBehavior(GhostFactory.createRedGhostShadow("Blinky"));
            case GameModel.PINK_GHOST_SPEEDY  -> modifyAmbushBehavior(GhostFactory.createPinkGhostAmbusher("Pinky"));
            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Sue");
            default -> throw new IllegalArgumentException("Illegal ghost personality: %d".formatted(personality));
        };
    }

    public static Ghost createGhost(byte personality, TerrainLayer terrain, House house, String startTileProperty) {
        final Ghost ghost = createGhost(personality);
        ghost.setHome(house);
        ghost.setStartPosition(halfTileRightOf(terrain.getTileProperty(startTileProperty)));
        return ghost;
    }

    private static Ghost modifyShadowBehavior(Ghost redGhost) {
        redGhost.setHuntingStrategy((GameLevel level, Float speed) -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final Vector2i tile = redGhost.computeTile();
            final boolean teleporting = terrain.isTileInPortalSpace(tile);
            if (teleporting) {
                redGhost.setSpeed(speed);
                redGhost.tryMovingOrTeleporting(level);
                return;
            }
            final boolean takeRandomDir = level.huntingTimer().phaseIndex() == 0
                && redGhost.isNewTileEntered()
                && terrain.isIntersection(tile);
            if (takeRandomDir) {
                selectRandomWishDir(redGhost, level);
                redGhost.setSpeed(speed);
                redGhost.tryMovingOrTeleporting(level);
            } else {
                // Normal behavior of red ghost
                final boolean chase = level.huntingTimer().isChasing() || redGhost.elroy().enabled();
                final Vector2i targetTile = chase
                    ? redGhost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(redGhost.personality());
                redGhost.setSpeed(speed);
                redGhost.tryMovingTowardsTargetTile(level, targetTile);
            }
        });
        return redGhost;
    }

    private static Ghost modifyAmbushBehavior(Ghost pinkGhost) {
        pinkGhost.setHuntingStrategy((GameLevel level, Float speed) -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final Vector2i tile = pinkGhost.computeTile();
            final boolean teleporting = terrain.isTileInPortalSpace(tile);
            if (teleporting) {
                pinkGhost.setSpeed(speed);
                pinkGhost.tryMovingOrTeleporting(level);
                return;
            }
            final boolean takeRandomDir = level.huntingTimer().phaseIndex() == 0
                && pinkGhost.isNewTileEntered()
                && terrain.isIntersection(tile);
            if (takeRandomDir) {
                selectRandomWishDir(pinkGhost, level);
                pinkGhost.setSpeed(speed);
                pinkGhost.tryMovingOrTeleporting(level);
            } else {
                final boolean chase = level.huntingTimer().isChasing();
                final Vector2i targetTile = chase
                    ? pinkGhost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(pinkGhost.personality());
                pinkGhost.setSpeed(speed);
                pinkGhost.tryMovingTowardsTargetTile(level, targetTile);
            }
        });
        return pinkGhost;
    }

    private static void selectRandomWishDir(Ghost ghost, GameLevel level) {
        for (final Direction dir : Direction.shuffled()) {
            final Vector2i neighbor = ghost.computeTile().plus(dir.vector());
            final boolean acceptable = dir != ghost.moveDir().opposite() && ghost.canAccessTile(level, neighbor);
            if (acceptable) {
                ghost.setWishDir(dir);
                Logger.debug("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
        }
    }
}
