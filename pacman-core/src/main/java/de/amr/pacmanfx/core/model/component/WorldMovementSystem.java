package de.amr.pacmanfx.core.model.component;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.model.world.WorldMap;
import org.tinylog.Logger;

import static de.amr.basics.math.Direction.UP;
import static java.util.Objects.requireNonNull;

public class WorldMovementSystem {

    public void reset(Actor actor) {
        final WorldMovement worldMovement = actor.worldMovement;

        worldMovement.info.clear();
        if (worldMovement.moveDir != null) {
            setMoveDir(actor, WorldMovement.DEFAULT_MOVE_DIR);  // updates velocity vector!
        }
        if (worldMovement.wishDir != null) {
            setWishDir(actor, WorldMovement.DEFAULT_WISH_DIR);
        }
        if (worldMovement.targetTile != null) {
            worldMovement.setTargetTile(WorldMovement.DEFAULT_TARGET_TILE);
        }
        worldMovement.canTeleport = WorldMovement.DEFAULT_CAN_TELEPORT;
        worldMovement.newTileEntered = true;
        worldMovement.turnBackRequested = false;
    }

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction (must not be null)
     */
    public void setMoveDir(Actor actor, Direction dir) {
        requireNonNull(dir);
        final Movement movement = actor.movement;
        final WorldMovement worldMovement = actor.worldMovement;

        if (worldMovement.moveDir == null && dir.equals(WorldMovement.DEFAULT_MOVE_DIR)) return;
        worldMovement.moveDirProperty().set(dir);
        double speed = movement.computeSpeed();
        movement.setVelocity(dir.vector().x() * speed, dir.vector().y() * speed);
    }

    /**
     * Sets the wish direction.
     *
     * @param dir the wish direction (must not be null)
     */
    public void setWishDir(Actor actor, Direction dir) {
        requireNonNull(dir);
        final WorldMovement worldMovement = actor.worldMovement;

        if (worldMovement.wishDir == null && dir.equals(WorldMovement.DEFAULT_WISH_DIR)) return;
        worldMovement.wishDirProperty().set(dir);
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
    public void placeAtTile(Actor actor, int tx, int ty, float ox, float oy) {
        final Position position = actor.position;
        final WorldMovement worldMovement = actor.worldMovement;

        final Vector2i prevTile = actor.computeTile();
        position.setX(tx * WorldMap.TS + ox);
        position.setY(ty * WorldMap.TS + oy);

        worldMovement.newTileEntered = !actor.computeTile().equals(prevTile);
    }

    /**
     * Places this actor exactly at the given tile coordinate. Updates the <code>newTileEntered</code> state.
     *
     * @param tx tile x-coordinate (grid column)
     * @param ty tile y-coordinate (grid row)
     */
    public void placeAtTile(Actor actor, int tx, int ty) {
        placeAtTile(actor, tx, ty, 0, 0);
    }

    /**
     * Places this actor centered over the given tile.
     *
     * @param tile tile where actor is placed
     */
    public void placeAtTile(Actor actor, Vector2i tile) {
        placeAtTile(actor, tile.x(), tile.y());
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     */
    public Vector2i tilesAhead(Actor actor, int numTiles) {
        final WorldMovement worldMovement = actor.worldMovement;

        return actor.computeTile().plus(worldMovement.moveDir().vector().scaled(numTiles));
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     * Overflow bug: In case the actor looks UP, additional {@code numTiles} tiles are added towards LEFT.
     */
    public Vector2i tilesAheadWithOverflowBug(Actor actor, int numTiles) {
        final WorldMovement worldMovement = actor.worldMovement;

        Vector2i ahead = tilesAhead(actor, numTiles);
        if (worldMovement.moveDir() == UP) {
            ahead = ahead.minus(numTiles, 0);
        }
        return ahead;
    }

    public void setSpeed(Actor actor, float speed) {
        final Movement movement = actor.movement;
        final WorldMovement worldMovement = actor.worldMovement;

        if (speed < 0) {
            throw new IllegalArgumentException("Speed must not be negative but is: " + speed);
        }
        movement.setVelocity(worldMovement.moveDir().vector().x() * speed, worldMovement.moveDir().vector().y() * speed);
    }

    public void navigateTowardsTarget(Actor actor, GameLevel level) {
        requireNonNull(level);
        final WorldMovement worldMovement = actor.worldMovement;

        if (!worldMovement.newTileEntered && worldMovement.info.moved || worldMovement.targetTile() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }

        final Vector2i currentTile = actor.computeTile();
        if (level.worldMap().terrainLayer().isTileInPortalSpace(currentTile)) {
            return;
        }
        Direction candidateDir = null;
        double minDistToTarget = Double.MAX_VALUE;
        for (Direction dir : WorldMovement.NAVIGATION_ORDER) {
            if (dir == worldMovement.moveDir().opposite()) {
                continue; // reversing the move direction is not allowed  (except to get out of dead-ends, see below)
            }
            final Vector2i neighborTile = currentTile.plus(dir.vector());
            if (actor.canAccessTile(level, neighborTile)) {
                double dist = neighborTile.euclideanDist(worldMovement.targetTile());
                if (dist < minDistToTarget) {
                    minDistToTarget = dist;
                    candidateDir = dir;
                }
            }
        }
        // if no direction could be determined, reverse (exit from dead-end)
        setWishDir(actor, candidateDir != null ? candidateDir : worldMovement.moveDir().opposite());
    }

    /**
     * Lets an actor move towards the given target tile.
     *
     * @param level      the game level we are in
     * @param targetTile target tile this actor tries to reach
     */
    public void tryMovingTowardsTargetTile(Actor actor, GameLevel level, Vector2i targetTile) {
        requireNonNull(level);
        final WorldMovement worldMovement = actor.worldMovement;

        if (targetTile != null) {
            worldMovement.setTargetTile(targetTile);
            navigateTowardsTarget(actor, level);
            tryMovingOrTeleporting(actor, level);
        }
    }

    /**
     * Tries moving or teleporting through the current level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     *
     * @param level the game level we are in
     */
    public void tryMovingOrTeleporting(Actor actor, GameLevel level) {
        requireNonNull(level);
        final WorldMovement worldMovement = actor.worldMovement;

        worldMovement.info.clear();
        if (worldMovement.canTeleport) {
            worldMovement.info.teleported = tryTeleporting(actor, level.worldMap().terrainLayer());
            if (worldMovement.info.teleported) {
                return;
            }
        }
        if (worldMovement.turnBackRequested && actor.canTurnBack()) {
            setWishDir(actor, worldMovement.moveDir().opposite());
            Logger.trace("{}: turned back at tile {}", actor.name(), actor.computeTile());
            worldMovement.turnBackRequested = false;
        }
        tryMovingTowards(actor, level, actor.computeTile(), worldMovement.wishDir());
        if (worldMovement.info.moved) {
            setMoveDir(actor, worldMovement.wishDir());
        } else {
            tryMovingTowards(actor, level, actor.computeTile(), worldMovement.moveDir());
        }
    }

    private boolean tryTeleporting(Actor actor, TerrainLayer terrain) {
        final WorldMovement worldMovement = actor.worldMovement;

        if (worldMovement.moveDir().isHorizontal()) {
            return terrain.horizontalPortals().stream()
                .filter(portal -> portal.tileY() == actor.computeTile().y())
                .findFirst()
                .map(portal -> portal.tryTeleporting(actor))
                .orElse(false);
        }
        return false; // no vertical teleporting yet
    }

    private void tryMovingTowards(Actor actor, GameLevel level, Vector2i tileBeforeMoving, Direction dir) {
        final Movement movement = actor.movement;
        final WorldMovement worldMovement = actor.worldMovement;

        final Vector2f newVelocity = dir.vector().scaled(movement.computeSpeed());
        final Vector2f touchPosition = actor.computeCenter().plus(dir.vector().scaled((float) WorldMap.HTS)).plus(newVelocity);
        final Vector2i touchedTile = WorldMap.computeTileAt(touchPosition);
        final boolean turn = dir.vector().isOrthogonalTo(worldMovement.moveDir().vector());

        if (!actor.canAccessTile(level, touchedTile)) {
            if (!turn) {
                placeAtTile(actor, actor.computeTile()); // adjust over tile (would move forward against wall)
            }
            Logger.debug("Cannot move %s into tile %s".formatted(dir, touchedTile));
            return;
        }

        if (turn) {
            float offset = dir.isHorizontal() ? actor.computeOffsetY() : actor.computeOffsetX();
            boolean atTurnPosition = Math.abs(offset) <= 1;
            if (atTurnPosition) {
                Logger.trace("Reached turn position ({})", actor.name());
                placeAtTile(actor, actor.computeTile()); // adjust over tile (starts moving around corner)
            } else {
                Logger.debug("Wants to take corner towards %s but not at turn position".formatted(dir));
                return;
            }
        }

        if (turn && worldMovement.corneringSpeedDelta != 0) {
            Vector2f cornerVelocity = newVelocity.plus(dir.vector().scaled(worldMovement.corneringSpeedDelta));
            Logger.trace("{} velocity around corner: {}", actor.name(), cornerVelocity.length());
            movement.setVelocity(cornerVelocity.x(), cornerVelocity.y());
            Movement.SYSTEM.moveAccelerated(actor);
            movement.setVelocity(newVelocity.x(), newVelocity.y());
        } else {
            movement.setVelocity(newVelocity.x(), newVelocity.y());
            Movement.SYSTEM.moveAccelerated(actor);
        }

        final Vector2i tileAfterMoving = actor.computeTile();
        worldMovement.newTileEntered = !tileBeforeMoving.equals(tileAfterMoving);

        worldMovement.info.moved = true;
        TerrainLayer terrainLayer = level.worldMap().terrainLayer();
        worldMovement.info.tunnelEntered = terrainLayer.isTunnel(tileAfterMoving)
            && !terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileBeforeMoving);
        worldMovement.info.tunnelLeft = !terrainLayer.isTunnel(tileAfterMoving)
            && terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileAfterMoving);

        Logger.debug("%5s (%.2f pixels)".formatted(dir, newVelocity.length()));
        if (worldMovement.info.tunnelEntered) {
            Logger.trace("{} entered tunnel", actor.name());
        }
        if (worldMovement.info.tunnelLeft) {
            Logger.trace("{} left tunnel", actor.name());
        }
    }
}
