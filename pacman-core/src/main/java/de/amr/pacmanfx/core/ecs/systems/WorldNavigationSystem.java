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

import static de.amr.basics.math.Direction.UP;
import static java.util.Objects.requireNonNull;

public class WorldNavigationSystem {

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

    public WorldNavigationSystem() {}

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction (must not be null)
     */
    public void setMoveDir(GameEntity actor, Direction dir) {
        requireNonNull(actor);
        requireNonNull(dir);

        final MovementComp movement = actor.reqComp(MovementComp.class);
        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        navigation.setMoveDir(dir);

        float speed = movement.speed();
        movement.setVelocity(dir.vector().x() * speed, dir.vector().y() * speed);
    }

    /**
     * Sets the wish direction.
     *
     * @param dir the wish direction (must not be null)
     */
    public void setWishDir(GameEntity actor, Direction dir) {
        requireNonNull(actor);
        requireNonNull(dir);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        navigation.setWishDir(dir);
    }

    public void clearTargetTile(GameEntity gameEntity) {
        gameEntity.optComp(WorldNavigationComp.class).ifPresent(comp -> comp.setTargetTile(null));
    }

    public void requestTurnBack(GameEntity actor) {
        requireNonNull(actor);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        navigation.setTurnBackRequested(true);
    }

    /**
     * Places this actor at the given tile coordinate with the given tile offsets. Updates the
     * <code>newTileEntered</code> state.
     *
     * @param tx tile x-coordinate (grid column)
     * @param ty tile y-coordinate (grid row)
     * @param ox x-offset inside tile
     * @param oy y-offset inside tile
     */
    public void placeAtTile(GameEntity actor, int tx, int ty, float ox, float oy) {
        requireNonNull(actor);

        final PositionComp position = actor.pos();
        final WorldNavigationComp worldNavigation = actor.reqComp(WorldNavigationComp.class);

        final Vector2i currentTile = actor.pos().tile();

        position.setX(tx * WorldMap.TS + ox);
        position.setY(ty * WorldMap.TS + oy);

        final Vector2i newTile = actor.pos().tile();

        worldNavigation.setNewTileEntered(!newTile.equals(currentTile));
    }

    /**
     * Places this actor exactly at the given tile coordinate. Updates the <code>newTileEntered</code> state.
     *
     * @param tx tile x-coordinate (grid column)
     * @param ty tile y-coordinate (grid row)
     */
    public void placeAtTile(GameEntity actor, int tx, int ty) {
        placeAtTile(actor, tx, ty, 0, 0);
    }

    /**
     * Places this actor centered over the given tile.
     *
     * @param actor an actor
     * @param tile tile where actor is placed
     */
    public void placeAtTile(GameEntity actor, Vector2i tile) {
        requireNonNull(tile);
        placeAtTile(actor, tile.x(), tile.y());
    }

    /**
     * Changes the velocity of the motor towards the current move direction of the given actor.
     *
     * @param actor an actor
     * @param speed the speed value
     */
    public void setMoveDirSpeed(GameEntity actor, float speed) {
        requireNonNull(actor);

        final MovementComp motor = actor.reqComp(MovementComp.class);
        final Direction moveDir = actor.reqComp(WorldNavigationComp.class).moveDir();
        motor.setVelocity(moveDir.vector().scaled(speed));
    }

    public void navigateTowardsTarget(GameEntity actor, GameLevel level, WorldMovementPolicy  movementPolicy) {
        requireNonNull(actor);
        requireNonNull(level);
        requireNonNull(movementPolicy);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);

        if (!navigation.isNewTileEntered() && navigation.info().moved || navigation.targetTile() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }

        final Vector2i currentTile = actor.pos().tile();
        if (level.worldMap().terrainLayer().isTileInPortalSpace(currentTile)) {
            return;
        }
        Direction candidateDir = null;
        double minDistToTarget = Double.MAX_VALUE;
        for (Direction dir : WorldNavigationComp.NAVIGATION_ORDER) {
            if (dir == navigation.moveDir().opposite()) {
                continue; // reversing the move direction is not allowed  (except to get out of dead-ends, see below)
            }
            final Vector2i neighborTile = currentTile.plus(dir.vector());
            if (movementPolicy.canAccessTile(level, actor, neighborTile)) {
                double dist = neighborTile.euclideanDist(navigation.targetTile());
                if (dist < minDistToTarget) {
                    minDistToTarget = dist;
                    candidateDir = dir;
                }
            }
        }
        // if no direction could be determined, reverse (exit from dead-end)
        setWishDir(actor, candidateDir != null ? candidateDir : navigation.moveDir().opposite());
    }

    public void tryMovingTowardsTargetTile(
        MovementSystem motor,
        GameEntity actor,
        GameLevel level,
        Vector2i targetTile,
        WorldMovementPolicy movementPolicy)
    {
        requireNonNull(actor);
        requireNonNull(level);
        requireNonNull(targetTile);
        requireNonNull(movementPolicy);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);
        navigation.setTargetTile(targetTile);
        navigateTowardsTarget(actor, level, movementPolicy);

        tryMovingOrTeleporting(motor, actor, level, movementPolicy);
    }

    /**
     * Tries moving or teleporting through the current level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    public void tryMovingOrTeleporting(
        MovementSystem motor,
        GameEntity actor,
        GameLevel level,
        WorldMovementPolicy movementPolicy)
    {
        requireNonNull(actor);
        requireNonNull(level);

        final WorldNavigationComp navigation = actor.reqComp(WorldNavigationComp.class);

        navigation.info().clear();

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
        tryMovingTowards(motor, actor, level, movementPolicy, actor.pos().tile(), navigation.wishDir());
        if (navigation.info().moved) {
            setMoveDir(actor, navigation.wishDir());
        } else {
            tryMovingTowards(motor, actor, level, movementPolicy, actor.pos().tile(), navigation.moveDir());
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

    private void tryMovingTowards(
        MovementSystem motor,
        GameEntity actor,
        GameLevel level,
        WorldMovementPolicy movementPolicy,
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
