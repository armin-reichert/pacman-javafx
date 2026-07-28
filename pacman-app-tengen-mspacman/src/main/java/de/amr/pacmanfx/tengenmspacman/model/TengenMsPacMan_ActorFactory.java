/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.House;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import org.tinylog.Logger;

import static de.amr.pacmanfx.core.model.world.WorldMap.halfTileRightOf;

public final class TengenMsPacMan_ActorFactory {

    public Pac createPacMan() {
        final var pacMan = new Pac("Pac-Man");
        pacMan.reset();
        return pacMan;
    }

    public Pac createMsPacMan() {
        final var msPacMan = new Pac("Ms. Pac-Man");
        msPacMan.reset();
        return msPacMan;
    }

    public Ghost createRedGhost() {
        final Ghost ghost = new Ghost(GameModel.RED_GHOST_SHADOW, "Blinky");
        registerCommonComponents(ghost);
        ghost.registerComponent(Elroy.class, new Elroy());
        ghost.reset();
        return ghost;
    }

    public Ghost createPinkGhost() {
        final Ghost ghost = new Ghost(GameModel.PINK_GHOST_SPEEDY, "Pinky");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public Ghost createCyanGhost() {
        final Ghost ghost = new Ghost(GameModel.CYAN_GHOST_BASHFUL, "Inky");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public Ghost createOrangeGhost() {
        final Ghost ghost = new Ghost(GameModel.ORANGE_GHOST_POKEY, "Sue");
        registerCommonComponents(ghost);
        ghost.reset();
        return ghost;
    }

    public void setTerrain(
        Ghost ghost,
        TerrainLayer terrain,
        House house,
        String startTileProperty)
    {
        ghost.setHouse(house);
        ghost.setStartPosition(halfTileRightOf(terrain.getTileProperty(startTileProperty)));
    }

    private void registerCommonComponents(Ghost ghost) {
        ghost.registerComponent(Movement.class, new Movement());
        ghost.registerComponent(WorldNavigation.class, new WorldNavigation());
        ghost.registerComponent(WorldMovementPolicy.class, new GhostWorldMovementPolicy());
        ghost.registerComponent(GhostStateComponent.class, new GhostStateComponent());
        ghost.registerComponent(SpriteAnim.class, new SpriteAnim());
        //TODO where does this belong?
        ghost.worldNavigation().corneringSpeedDelta = -1.25f;
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
        final WorldMovementSystem navigator = gameContext.systems().navigator;
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
                navigator.setWishDir(ghost, dir);
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
        return dir != ghost.worldNavigation().moveDir().opposite()
            && policy.canAccessTile(gameContext, ghost, neighborTile);
    }
}
