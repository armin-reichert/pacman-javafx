/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.GhostFactory;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
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
    public static Ghost createGhost(GameContext gameContext, byte personality) {
        return switch (personality) {
            case GameModel.RED_GHOST_SHADOW   -> modifyShadowBehavior(GhostFactory.createRedGhostShadow("Blinky"));
            case GameModel.PINK_GHOST_SPEEDY  -> modifyAmbushBehavior(GhostFactory.createPinkGhostAmbusher("Pinky"));
            case GameModel.CYAN_GHOST_BASHFUL -> GhostFactory.createCyanGhostBashful("Inky");
            case GameModel.ORANGE_GHOST_POKEY -> GhostFactory.createOrangeGhostPokey("Sue");
            default -> throw new IllegalArgumentException();
        };
    }

    public static Ghost createGhost(GameContext gameContext, byte personality, House house, TerrainLayer terrain, String startTileProperty) {
        final Ghost ghost = TengenMsPacMan_ActorFactory.createGhost(gameContext, personality);
        ghost.setHouse(house);
        if (ghost.personality() == GameModel.RED_GHOST_SHADOW) {
            ghost.setStartPosition(WorldMap.halfTileRightOf(terrain.getTileProperty(startTileProperty)));
        } else {
            // The ghosts starting inside the house sit at the *bottom*!
            ghost.setStartPosition(WorldMap.halfTileRightOf(terrain.getTileProperty(startTileProperty)).plus(0, WorldMap.HTS));
        }
        return ghost;
    }

    private static Ghost modifyShadowBehavior(Ghost ghost) {

        //TODO create strategy class

        /*
        ghost.setHuntingStrategy((GameContext gameContext, Float speed) -> {
            final GameLevel level = gameContext.assertLevel();
            final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
            final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean firstScatterPhase = level.huntingRules().phaseIndex() == 0;
            final boolean takeRandomDir = ghost.worldMovement().isNewTileEntered() && terrain.isIntersection(ghostTile);

            if (firstScatterPhase && takeRandomDir) {
                selectRandomWishDir(ghost, gameContext);
                worldMovementSystem.setSpeed(ghost, speed);
                worldMovementSystem.tryMovingOrTeleporting(ghost, gameContext);
            } else {
                // Normal behavior of red ghost
                final boolean chase = level.huntingRules().isChasing() || ghost.assertComponent(Elroy.class).enabled();
                final Vector2i targetTile = chase
                    ? ghost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(ghost.personality());
                worldMovementSystem.setSpeed(ghost, speed);
                worldMovementSystem.tryMovingTowardsTargetTile(ghost, gameContext, targetTile);
            }
        });

         */
        return ghost;
    }

    private static Ghost modifyAmbushBehavior(Ghost ghost) {

        //TODO create strategy class

        /*
        ghost.setHuntingStrategy((GameContext gameContext, Float speed) -> {
            final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
            final GameLevel level = gameContext.assertLevel();
            final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final boolean firstScatterPhase = level.huntingRules().phaseIndex() == 0;
            final boolean takeRandomDir = ghost.worldMovement().isNewTileEntered() && terrain.isIntersection(ghostTile);

            if (firstScatterPhase && takeRandomDir) {
                selectRandomWishDir(ghost, gameContext);
                worldMovementSystem.setSpeed(ghost, speed);
                worldMovementSystem.tryMovingOrTeleporting(ghost, gameContext);
            } else {
                final boolean chase = level.huntingRules().isChasing();
                final Vector2i targetTile = chase
                    ? ghost.chasingTargetTileStrategy().apply(level)
                    : terrain.ghostScatterTile(ghost.personality());
                worldMovementSystem.setSpeed(ghost, speed);
                worldMovementSystem.tryMovingTowardsTargetTile(ghost, gameContext, targetTile);
            }
        });

         */
        return ghost;
    }

    private static void selectRandomWishDir(Ghost ghost, GameContext gameContext) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().navigator;
        final GameLevel level = gameContext.assertLevel();
        final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
        final boolean teleporting = level.worldMap().terrainLayer().isTileInPortalSpace(ghostTile);

        if (teleporting) {
            return;
        }
        int dirsTried = 0;
        Direction dir = Direction.random();
        while (++dirsTried <= 4) {
            if (isAcceptableWishDir(gameContext, ghost, dir)) {
                worldMovementSystem.setWishDir(ghost, dir);
                Logger.debug("{} selects random wish direction {}", ghost.name(), dir);
                break;
            }
            Logger.debug("{} rejects wish dir {}", ghost.name(), dir);
            dir = dir.nextClockwise();
        }
    }

    private static boolean isAcceptableWishDir(GameContext gameContext, Ghost ghost, Direction dir) {
        final WorldMovementPolicy policy = ghost.assertComponent(WorldMovementPolicy.class);

        final Vector2i ghostTile = WorldMovementSystem.computeTile(ghost);
        final Vector2i neighborTile = ghostTile.plus(dir.vector());
        return dir != ghost.worldMovement().moveDir().opposite()
            && policy.canAccessTile(gameContext, ghost, neighborTile);
    }
}
