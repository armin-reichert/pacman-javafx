/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs.systems;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.basics.math.Direction.UP;
import static java.util.Objects.requireNonNull;

public class WorldNavigationSystem {

    /** Order in which directions are selected when navigation decision is met. */
    public static final List<Direction> NAVIGATION_ORDER = List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT);

    /**
     * @param actor an actor that can move through the world
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     */
    public static Vector2i tilesAhead(GameEntity actor, int numTiles) {
        requireNonNull(actor);

        final WorldNavigationComp worldNavigation = actor.reqComp(WorldNavigationComp.class);
        final Vector2i translateVector = worldNavigation.moveDir().vector().scaled(numTiles);
        return actor.pos().tile().plus(translateVector);
    }

    /**
     * Simulates the overflow bug in the original Arcade game: 
     * In case the actor looks UP, additional {@code numTiles} tiles are added towards LEFT.
     * 
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     */
    public static Vector2i tilesAheadWithOverflowBug(GameEntity actor, int numTiles) {
        requireNonNull(actor);

        final WorldNavigationComp worldNavigation = actor.reqComp(WorldNavigationComp.class);
        return worldNavigation.moveDir() == UP
            ? tilesAhead(actor, numTiles).minus(numTiles, 0)
            : tilesAhead(actor, numTiles);
    }

    // -------------------------------------------------------------

    private final MovementSystem motor;

    public WorldNavigationSystem(MovementSystem motor) {
        this.motor = requireNonNull(motor);
    }

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction (must not be null)
     */
    public void setMoveDir(GameEntity actor, Direction dir) {
        requireNonNull(actor);
        requireNonNull(dir);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        if (navigation.moveDir() != dir) {
            navigation.setMoveDir(dir);
            final MovementComp movement = actor.reqComp(MovementComp.class);
            movement.setVelocity(dir.vector().scaled(movement.speed()));
        }
    }

    /**
     * Sets the wish direction for the given actor. Asserts actor has navigation capability.
     *
     * @param actor an actor with navigation capability
     * @param dir the wish direction (not null)
     */
    public void setWishDir(GameEntity actor, Direction dir) {
        requireNonNull(actor);
        requireNonNull(dir);
        actor.reqComp(WorldNavigationComp.class).setWishDir(dir);
    }

    /**
     * Sets the current target tile for the given actor. Asserts actor has navigation capability.
     *
     * @param actor an actor with navigation capability
     * @param tile target tile (not null)
     */
    public void setTargetTile(GameEntity actor, Vector2i tile) {
        requireNonNull(actor);
        requireNonNull(tile);
        actor.reqComp(WorldNavigationComp.class).setTargetTile(tile);
    }

    /**
     * Clears the current target tile for the given actor. Asserts actor has navigation capability.
     *
     * @param actor an actor with navigation capability
     */
    public void clearTargetTile(GameEntity actor) {
        requireNonNull(actor);
        actor.reqComp(WorldNavigationComp.class).setTargetTile(null);
    }

    /**
     * Requests the actor to turn around by 180 degrees at the next occasion.
     *
     * @param actor an actor with navigation capability
     */
    public void requestTurnBack(GameEntity actor) {
        requireNonNull(actor);
        actor.reqComp(WorldNavigationComp.class).setTurnBackRequested(true);
    }

    /**
     * Places this actor at the given tile coordinate with the given tile offsets. Updates the
     * <code>newTileEntered</code> state.
     *
     * @param actor an actor with navigation capability
     * @param tx tile x-coordinate (grid column)
     * @param ty tile y-coordinate (grid row)
     * @param ox x-offset inside tile
     * @param oy y-offset inside tile
     */
    public void placeAtTile(GameEntity actor, int tx, int ty, float ox, float oy) {
        requireNonNull(actor);

        final PositionComp position = actor.pos();
        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        final Vector2i tileBefore = actor.pos().tile();

        position.setX(tx * WorldMap.TS + ox);
        position.setY(ty * WorldMap.TS + oy);

        final Vector2i tileAfter = actor.pos().tile();
        navigation.setNewTileEntered(!tileAfter.equals(tileBefore));
    }

    /**
     * Places this actor exactly at the given tile coordinate. Updates the <code>newTileEntered</code> state.
     *
     * @param actor an actor with navigation capability
     * @param tx tile x-coordinate (grid column)
     * @param ty tile y-coordinate (grid row)
     */
    public void placeAtTile(GameEntity actor, int tx, int ty) {
        placeAtTile(actor, tx, ty, 0, 0);
    }

    /**
     * Places this actor centered over the given tile.
     *
     * @param actor an actor with navigation capability
     * @param tile tile where actor is placed (not null)
     */
    public void placeAtTile(GameEntity actor, Vector2i tile) {
        requireNonNull(tile);
        placeAtTile(actor, tile.x(), tile.y());
    }

    /**
     * Changes the velocity of the motor towards the current move direction of the given actor.
     *
     * @param actor an actor with navigation capability
     * @param speed the speed in pixels/tick
     */
    public void setMoveDirSpeed(GameEntity actor, float speed) {
        requireNonNull(actor);

        final MovementComp movement = actor.reqComp(MovementComp.class);
        final Direction moveDir = actor.reqComp(WorldNavigationComp.class).moveDir();
        movement.setVelocity(moveDir.vector().scaled(speed));
    }

    /**
     * Computes the wish direction suited to reach an actor's current target tile.
     *
     * @param actor an actor with navigation capability
     * @param level the game level
     * @param movementPolicy the movement policy of the actor
     * @param <A> the actor type
     */
    public <A extends GameEntity> void navigateActorTowardsCurrentTarget(A actor, GameLevel level, WorldMovementPolicy<A>  movementPolicy) {
        requireNonNull(actor);
        requireNonNull(level);
        requireNonNull(movementPolicy);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);

        if (!navigation.isNewTileEntered() && navigation.info().moved || navigation.targetTile() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }

        final Vector2i tileBefore = actor.pos().tile();
        if (level.worldMap().terrainLayer().isTileInPortalSpace(tileBefore)) {
            return;
        }

        // Computes for each tile accessible from the current tile the distance to the current target tile and
        // selects the one with the minimum distance. Tiles are checked in the fixed navigation order which creates
        // the characteristic behavior observed in the Arcade game.
        // We also allow maps with dead-ends (custom maps), so the actor is allowed to turn-back if stuck in one.
        final Direction backwards = navigation.moveDir().opposite();
        Direction candidate = null;
        double currentMinDist = Double.MAX_VALUE;
        for (Direction dir : NAVIGATION_ORDER) {
            if (dir == backwards) {
                continue; // reversing the move direction is not allowed  (except to get out of dead-ends, see below)
            }
            final Vector2i neighborTile = tileBefore.plus(dir.vector());
            if (movementPolicy.canAccessTile(level, actor, neighborTile)) {
                final double distanceToTarget = neighborTile.euclideanDist(navigation.targetTile());
                if (distanceToTarget < currentMinDist) {
                    currentMinDist = distanceToTarget;
                    candidate = dir;
                }
            }
        }

        // if no direction towards the current target tile could be determined, reverse (exit from dead-end)
        setWishDir(actor, candidate != null ? candidate : backwards);
    }

    public <E extends GameEntity> void tryMovingTowardsTargetTile(
        E actor,
        GameLevel level,
        Vector2i targetTile,
        WorldMovementPolicy<E> movementPolicy)
    {
        requireNonNull(actor);
        requireNonNull(level);
        requireNonNull(targetTile);
        requireNonNull(movementPolicy);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);

        if (navigation.isPaused()) {
            return;
        }

        navigation.setTargetTile(targetTile);
        navigateActorTowardsCurrentTarget(actor, level, movementPolicy);

        tryMovingOrTeleporting(level, actor, movementPolicy);
    }

    /**
     * Tries moving or teleporting through the current level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    public <E extends GameEntity> void tryMovingOrTeleporting(
        GameLevel level,
        E actor,
        WorldMovementPolicy<E> movementPolicy)
    {
        requireNonNull(actor);
        requireNonNull(level);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        navigation.info().clear();

        if (navigation.isPaused()) {
            return;
        }

        if (navigation.canTeleport()) {
            navigation.info().teleportStarted = tryTeleporting(actor, level.worldMap().terrainLayer());
            if (navigation.info().teleportStarted) {
                navigation.setInTeleportingSpace(true);
                return;
            }
        }

        final Vector2f center = actor.pos().bodyCenter();
        final int leftBorder = 0;
        final int rightBorder = level.worldMap().numCols() * WorldMap.TS;
        navigation.setInTeleportingSpace(center.x() < leftBorder || center.x() > rightBorder);

        if (navigation.isTurnBackRequested() && movementPolicy.canTurnBack(actor)) {
            setWishDir(actor, navigation.moveDir().opposite());
            navigation.setTurnBackRequested(false);
        }
        tryMovingTowards(actor, level, movementPolicy, actor.pos().tile(), navigation.wishDir());
        if (navigation.info().moved) {
            setMoveDir(actor, navigation.wishDir());
        } else {
            tryMovingTowards(actor, level, movementPolicy, actor.pos().tile(), navigation.moveDir());
        }
    }

    private boolean tryTeleporting(GameEntity actor, TerrainLayer terrain) {
        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);

        if (navigation.moveDir().isHorizontal()) {
            return terrain.horizontalPortals().stream()
                .filter(portal -> portal.tileY() == actor.pos().tile().y())
                .findFirst()
                .map(portal -> portal.tryTeleporting(this, actor))
                .orElse(false);
        }
        return false; // no vertical teleporting yet
    }

    private <E extends GameEntity> void tryMovingTowards(
        E actor,
        GameLevel level,
        WorldMovementPolicy<E> movementPolicy,
        Vector2i tileBeforeMoving,
        Direction dir)
    {
        final MovementComp movement = actor.reqComp(MovementComp.class);
        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);

        final Vector2f newVelocity = dir.vector().scaled(movement.speed());
        final Vector2f touchPosition = actor.pos().bodyCenter().plus(dir.vector().scaled((float) WorldMap.HTS)).plus(newVelocity);
        final Vector2i touchedTile = PositionSystem.computeTileAt(touchPosition);
        final boolean turn = dir.vector().isOrthogonalTo(navigation.moveDir().vector());

        if (!movementPolicy.canAccessTile(level, actor, touchedTile)) {
            if (!turn) {
                placeAtTile(actor, actor.pos().tile()); // adjust over tile (would move forward against wall)
            }
            Logger.debug("Cannot move %s into tile %s".formatted(dir, touchedTile));
            return;
        }

        if (turn) {
            final Vector2i tileOffset = PositionSystem.computeTileOffset(actor.pos().asVector2f());
            final float offset = dir.isHorizontal() ? tileOffset.y() : tileOffset.x();
            final boolean atTurnPosition = Math.abs(offset) <= 1;
            if (atTurnPosition) {
                placeAtTile(actor, actor.pos().tile()); // adjust over tile (starts moving around corner)
            } else {
                Logger.debug("Wants to take corner towards %s but not at turn position".formatted(dir));
                return;
            }
        }

        if (turn && navigation.corneringSpeedDelta != 0) {
            final Vector2f cornerVelocity = newVelocity.plus(dir.vector().scaled(navigation.corneringSpeedDelta));
            Logger.trace("{} velocity around corner: {}", actor.name(), cornerVelocity.length());
            motor.setVelocity(actor, cornerVelocity.x(), cornerVelocity.y());
            motor.move(actor);
            // Reset velocity after moving
            motor.setVelocity(actor, newVelocity.x(), newVelocity.y());
        } else {
            motor.setVelocity(actor, newVelocity.x(), newVelocity.y());
            motor.move(actor);
        }

        final Vector2i tileAfterMoving = actor.pos().tile();
        navigation.setNewTileEntered(!tileBeforeMoving.equals(tileAfterMoving));

        navigation.info().moved = true;

        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();

        navigation.info().tunnelEntered = terrainLayer.isTunnel(tileAfterMoving)
            && !terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileBeforeMoving);

        navigation.info().tunnelLeft = !terrainLayer.isTunnel(tileAfterMoving)
            && terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileAfterMoving);

        Logger.debug("%5s (%.2f pixels)".formatted(dir, newVelocity.length()));
        if (navigation.info().tunnelEntered) {
            Logger.trace("{} entered tunnel", actor.name());
        }
        if (navigation.info().tunnelLeft) {
            Logger.trace("{} left tunnel", actor.name());
        }
    }
}
