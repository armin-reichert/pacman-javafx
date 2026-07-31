/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.common;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.common.PositionComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.model.world.WorldMap;
import org.tinylog.Logger;

import static de.amr.basics.math.Direction.UP;
import static java.util.Objects.requireNonNull;

public class WorldNavigationSystem {

    public static Vector2f computeCenter(GameEntity actor) {
        requireNonNull(actor);
        final PositionComp position = actor.pos();
        return new Vector2f(position.x() + WorldMap.HTS, position.y() + WorldMap.HTS);
    }

    public static Vector2i computeTile(GameEntity actor) {
        requireNonNull(actor);
        final PositionComp position = actor.pos();
        final float cx = position.x() + WorldMap.HTS;
        final float cy = position.y() + WorldMap.HTS;
        return WorldMap.computeTileAt(cx, cy);
    }

    /**
     * @return offset of actor position relative to current tile: (0, 0) if centered, range: [-4, +4)
     */
    public static Vector2f computeTileOffset(GameEntity actor) {
        requireNonNull(actor);
        final PositionComp position = actor.pos();
        final Vector2i tile = computeTile(actor);
        return new Vector2f(position.x() - tile.x() * WorldMap.TS, position.y() - tile.y() * WorldMap.TS);
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     */
    public static Vector2i tilesAhead(GameEntity actor, int numTiles) {
        requireNonNull(actor);
        final WorldNavigationComp worldNavigation = actor.requireComponent(WorldNavigationComp.class);

        return computeTile(actor).plus(worldNavigation.moveDir().vector().scaled(numTiles));
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     * Overflow bug: In case the actor looks UP, additional {@code numTiles} tiles are added towards LEFT.
     */
    public static Vector2i tilesAheadWithOverflowBug(GameEntity actor, int numTiles) {
        requireNonNull(actor);

        final WorldNavigationComp worldNavigation = actor.requireComponent(WorldNavigationComp.class);

        Vector2i ahead = tilesAhead(actor, numTiles);
        if (worldNavigation.moveDir() == UP) {
            ahead = ahead.minus(numTiles, 0);
        }
        return ahead;
    }

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

        final MovementComp movement = actor.requireComponent(MovementComp.class);
        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        if (navigation.moveDir() == null && dir.equals(WorldNavigationComp.DEFAULT_MOVE_DIR)) return;
        navigation.moveDirProperty().set(dir);
        float speed = movement.speed();
        motor.setVelocity(actor, dir.vector().x() * speed, dir.vector().y() * speed);
    }

    /**
     * Sets the wish direction.
     *
     * @param dir the wish direction (must not be null)
     */
    public void setWishDir(GameEntity actor, Direction dir) {
        requireNonNull(actor);
        requireNonNull(dir);

        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        if (navigation.wishDir() == null && dir.equals(WorldNavigationComp.DEFAULT_WISH_DIR)) return;
        navigation.wishDirProperty().set(dir);
    }

    public void requestTurnBack(GameEntity actor) {
        requireNonNull(actor);

        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);
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
        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        final Vector2i prevTile = computeTile(actor);
        position.setX(tx * WorldMap.TS + ox);
        position.setY(ty * WorldMap.TS + oy);

        navigation.setNewTileEntered(!computeTile(actor).equals(prevTile));
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
     * @param tile tile where actor is placed
     */
    public void placeAtTile(GameEntity actor, Vector2i tile) {
        placeAtTile(actor, tile.x(), tile.y());
    }

    public void setSpeed(GameEntity actor, float speed) {
        requireNonNull(actor);

        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        if (speed < 0) {
            throw new IllegalArgumentException("Speed must not be negative but is: " + speed);
        }
        final Vector2i moveDirVec = navigation.moveDir().vector();
        motor.setVelocity(actor, moveDirVec.x() * speed, moveDirVec.y() * speed);
    }

    public void navigateTowardsTarget(GameEntity actor, GameLevel level, WorldMovementPolicy  movementPolicy) {
        requireNonNull(actor);
        requireNonNull(level);
        requireNonNull(movementPolicy);

        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        if (!navigation.isNewTileEntered() && navigation.info.moved || navigation.targetTile() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }

        final Vector2i currentTile = computeTile(actor);
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

    public void tryMovingTowardsTargetTile(GameEntity actor, GameLevel level, Vector2i targetTile, WorldMovementPolicy  movementPolicy) {
        requireNonNull(actor);
        requireNonNull(level);
        requireNonNull(targetTile);
        requireNonNull(movementPolicy);

        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);
        navigation.setTargetTile(targetTile);
        navigateTowardsTarget(actor, level, movementPolicy);

        tryMovingOrTeleporting(actor, level, movementPolicy);
    }

    /**
     * Tries moving or teleporting through the current level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    public void tryMovingOrTeleporting(GameEntity actor, GameLevel level, WorldMovementPolicy movementPolicy) {
        requireNonNull(actor);
        requireNonNull(level);

        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        navigation.info.clear();
        if (navigation.canTeleport()) {
            navigation.info.teleported = tryTeleporting(actor, level.worldMap().terrainLayer());
            if (navigation.info.teleported) {
                return;
            }
        }
        if (navigation.isTurnBackRequested() && movementPolicy.canTurnBack(actor)) {
            setWishDir(actor, navigation.moveDir().opposite());
            navigation.setTurnBackRequested(false);
        }
        tryMovingTowards(actor, level, movementPolicy, computeTile(actor), navigation.wishDir());
        if (navigation.info.moved) {
            setMoveDir(actor, navigation.wishDir());
        } else {
            tryMovingTowards(actor, level, movementPolicy, computeTile(actor), navigation.moveDir());
        }
    }

    private boolean tryTeleporting(GameEntity actor, TerrainLayer terrain) {
        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        if (navigation.moveDir().isHorizontal()) {
            return terrain.horizontalPortals().stream()
                .filter(portal -> portal.tileY() == computeTile(actor).y())
                .findFirst()
                .map(portal -> portal.tryTeleporting(this, actor))
                .orElse(false);
        }
        return false; // no vertical teleporting yet
    }

    private void tryMovingTowards(GameEntity actor, GameLevel level, WorldMovementPolicy movementPolicy, Vector2i tileBeforeMoving, Direction dir) {
        final MovementComp movement = actor.requireComponent(MovementComp.class);
        final WorldNavigationComp navigation = actor.requireComponent(WorldNavigationComp.class);

        final Vector2f newVelocity = dir.vector().scaled(movement.speed());
        final Vector2f touchPosition = computeCenter(actor).plus(dir.vector().scaled((float) WorldMap.HTS)).plus(newVelocity);
        final Vector2i touchedTile = WorldMap.computeTileAt(touchPosition);
        final boolean turn = dir.vector().isOrthogonalTo(navigation.moveDir().vector());

        if (!movementPolicy.canAccessTile(level, actor, touchedTile)) {
            if (!turn) {
                placeAtTile(actor, computeTile(actor)); // adjust over tile (would move forward against wall)
            }
            Logger.debug("Cannot move %s into tile %s".formatted(dir, touchedTile));
            return;
        }

        if (turn) {
            final Vector2f tileOffset = computeTileOffset(actor);
            final float offset = dir.isHorizontal() ? tileOffset.y() : tileOffset.x();
            final boolean atTurnPosition = Math.abs(offset) <= 1;
            if (atTurnPosition) {
                placeAtTile(actor, computeTile(actor)); // adjust over tile (starts moving around corner)
            } else {
                Logger.debug("Wants to take corner towards %s but not at turn position".formatted(dir));
                return;
            }
        }

        if (turn && navigation.corneringSpeedDelta != 0) {
            final Vector2f cornerVelocity = newVelocity.plus(dir.vector().scaled(navigation.corneringSpeedDelta));
            Logger.trace("{} velocity around corner: {}", actor.name(), cornerVelocity.length());
            motor.setVelocity(actor, cornerVelocity.x(), cornerVelocity.y());
            motor.moveAccelerated(actor);
            // Reset velocity after moving
            motor.setVelocity(actor, newVelocity.x(), newVelocity.y());
        } else {
            motor.setVelocity(actor, newVelocity.x(), newVelocity.y());
            motor.moveAccelerated(actor);
        }

        final Vector2i tileAfterMoving = computeTile(actor);
        navigation.setNewTileEntered(!tileBeforeMoving.equals(tileAfterMoving));

        navigation.info.moved = true;

        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();

        navigation.info.tunnelEntered = terrainLayer.isTunnel(tileAfterMoving)
            && !terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileBeforeMoving);

        navigation.info.tunnelLeft = !terrainLayer.isTunnel(tileAfterMoving)
            && terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileAfterMoving);

        Logger.debug("%5s (%.2f pixels)".formatted(dir, newVelocity.length()));
        if (navigation.info.tunnelEntered) {
            Logger.trace("{} entered tunnel", actor.name());
        }
        if (navigation.info.tunnelLeft) {
            Logger.trace("{} left tunnel", actor.name());
        }
    }
}
