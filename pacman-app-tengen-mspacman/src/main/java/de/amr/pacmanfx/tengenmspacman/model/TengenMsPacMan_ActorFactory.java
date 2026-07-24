/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostFactory;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.model.world.WorldMap;
import org.tinylog.Logger;

public final class TengenMsPacMan_ActorFactory {

    private TengenMsPacMan_ActorFactory() {}

    public static Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public static Pac createMsPacMan() {
        final var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    /**
     * In Arcade Ms. Pac-Man, Blinky and Pinky move randomly during the *first* scatter phase. Some say,
     * the original intention had been to randomize the scatter target of *all* ghosts but because of a bug,
     * only the scatter target of Blinky and Pinky would have been affected. Who knows?
     * <p>
     * I use the same behavior here, however I do not know what the real Tengen implementation does.
     * </p>
     */
    public static Ghost createGhost(byte personality) {
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> modifyShadowBehavior(GhostFactory.createRedGhostShadow("Blinky"));
            case GameModel.PINK_GHOST_SPEEDY  -> modifyAmbushBehavior(GhostFactory.createPinkGhostAmbusher("Pinky"));
            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Sue");
            default -> throw new IllegalArgumentException();
        };
    }

    public static Ghost createGhost(byte personality, House house, TerrainLayer terrain, String startTileProperty) {
        final Ghost ghost = TengenMsPacMan_ActorFactory.createGhost(personality);
        ghost.setHome(house);
        if (ghost.personality() == GameModel.RED_GHOST_SHADOW) {
            ghost.setStartPosition(WorldMap.halfTileRightOf(terrain.getTileProperty(startTileProperty)));
        } else {
            // The ghosts starting inside the house sit at the *bottom*!
            ghost.setStartPosition(WorldMap.halfTileRightOf(terrain.getTileProperty(startTileProperty)).plus(0, WorldMap.HTS));
        }
        return ghost;
    }


    private static Ghost modifyShadowBehavior(Ghost ghost) {
        ghost.setHuntingStrategy((GameLevel level, Float speed) -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean firstScatterPhase = level.huntingRules().phaseIndex() == 0;
            final boolean takeRandomDir = ghost.isNewTileEntered() && terrain.isIntersection(ghost.computeTile());
            if (firstScatterPhase && takeRandomDir) {
                selectRandomWishDir(ghost, level);
                ghost.setSpeed(speed);
                ghost.tryMovingOrTeleporting(level);
            } else {
                // Normal behavior of red ghost
                final boolean chase = level.huntingRules().isChasing() || ghost.elroy().enabled();
                final Vector2i targetTile = chase
                    ? ghost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(ghost.personality());
                ghost.setSpeed(speed);
                ghost.tryMovingTowardsTargetTile(level, targetTile);
            }
        });
        return ghost;
    }

    private static Ghost modifyAmbushBehavior(Ghost ghost) {
        ghost.setHuntingStrategy((GameLevel level, Float speed) -> {
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean firstScatterPhase = level.huntingRules().phaseIndex() == 0;
            final boolean takeRandomDir = ghost.isNewTileEntered() && terrain.isIntersection(ghost.computeTile());
            if (firstScatterPhase && takeRandomDir) {
                selectRandomWishDir(ghost, level);
                ghost.setSpeed(speed);
                ghost.tryMovingOrTeleporting(level);
            } else {
                final boolean chase = level.huntingRules().isChasing();
                final Vector2i targetTile = chase
                    ? ghost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(ghost.personality());
                ghost.setSpeed(speed);
                ghost.tryMovingTowardsTargetTile(level, targetTile);
            }
        });
        return ghost;
    }

    private static void selectRandomWishDir(Ghost ghost, GameLevel level) {
        final Vector2i tile = ghost.computeTile();
        final boolean teleporting = level.worldMap().terrainLayer().isTileInPortalSpace(tile);
        if (teleporting) {
            return;
        }
        int dirsTried = 0;
        Direction dir = Direction.random();
        while (++dirsTried <= 4) {
            if (isAcceptableWishDir(level, ghost, dir)) {
                ghost.setWishDir(dir);
                Logger.debug("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
            dir = dir.nextClockwise();
        }
    }

    private static boolean isAcceptableWishDir(GameLevel level, Ghost ghost, Direction dir) {
        final Vector2i neighborTile = ghost.computeTile().plus(dir.vector());
        return dir != ghost.moveDir().opposite() && ghost.canAccessTile(level, neighborTile);
    }
}
