package de.amr.pacmanfx.core.model.systems;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.common.Position;
import de.amr.pacmanfx.core.model.component.world.WorldMovement;
import de.amr.pacmanfx.core.model.component.world.WorldMovementPolicy;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.TerrainLayer;
import de.amr.pacmanfx.core.model.world.WorldMap;
import org.tinylog.Logger;

import static de.amr.basics.math.Direction.UP;
import static java.util.Objects.requireNonNull;

public class WorldMovementSystem {

    public static Vector2f computeCenter(Actor actor) {
        requireNonNull(actor);
        final Position position = actor.position();
        return new Vector2f(position.x + WorldMap.HTS, position.y + WorldMap.HTS);
    }

    public static Vector2i computeTile(Actor actor) {
        requireNonNull(actor);
        final Position position = actor.position();
        final float cx = position.x + WorldMap.HTS;
        final float cy = position.y + WorldMap.HTS;
        return WorldMap.computeTileAt(cx, cy);
    }

    /**
     * @return offset of actor position relative to current tile: (0, 0) if centered, range: [-4, +4)
     */
    public static Vector2f computeTileOffset(Actor actor) {
        requireNonNull(actor);
        final Position position = actor.position();
        final Vector2i tile = computeTile(actor);
        return new Vector2f(position.x - tile.x() * WorldMap.TS, position.y - tile.y() * WorldMap.TS);
    }

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction (must not be null)
     */
    public void setMoveDir(Actor actor, Direction dir) {
        requireNonNull(actor);
        requireNonNull(dir);

        final Movement movement = actor.movement();
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (worldMovement.moveDir() == null && dir.equals(WorldMovement.DEFAULT_MOVE_DIR)) return;
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
        requireNonNull(actor);
        requireNonNull(dir);

        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (worldMovement.wishDir() == null && dir.equals(WorldMovement.DEFAULT_WISH_DIR)) return;
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
        requireNonNull(actor);

        final Position position = actor.position();
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        final Vector2i prevTile = computeTile(actor);
        position.setX(tx * WorldMap.TS + ox);
        position.setY(ty * WorldMap.TS + oy);

        worldMovement.setNewTileEntered(!computeTile(actor).equals(prevTile));
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
        requireNonNull(actor);
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        return computeTile(actor).plus(worldMovement.moveDir().vector().scaled(numTiles));
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     * Overflow bug: In case the actor looks UP, additional {@code numTiles} tiles are added towards LEFT.
     */
    public Vector2i tilesAheadWithOverflowBug(Actor actor, int numTiles) {
        requireNonNull(actor);

        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        Vector2i ahead = tilesAhead(actor, numTiles);
        if (worldMovement.moveDir() == UP) {
            ahead = ahead.minus(numTiles, 0);
        }
        return ahead;
    }

    public void setSpeed(Actor actor, float speed) {
        requireNonNull(actor);

        final Movement movement = actor.movement();
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (speed < 0) {
            throw new IllegalArgumentException("Speed must not be negative but is: " + speed);
        }
        movement.setVelocity(worldMovement.moveDir().vector().x() * speed, worldMovement.moveDir().vector().y() * speed);
    }

    public void navigateTowardsTarget(Actor actor, GameContext gameContext) {
        requireNonNull(actor);
        requireNonNull(gameContext);

        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
        final WorldMovementPolicy worldMovementPolicy = actor.assertComponent(WorldMovementPolicy.class);

        if (!worldMovement.isNewTileEntered() && worldMovement.info.moved || worldMovement.targetTile() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }

        final GameLevel level = gameContext.assertLevel();
        final Vector2i currentTile = computeTile(actor);
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
            if (worldMovementPolicy.canAccessTile(gameContext, actor, neighborTile)) {
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
     * @param actor         the actor
     * @param gameContext   the game context (asserts level exists)
     * @param targetTile    target tile this actor tries to reach
     */
    public void tryMovingTowardsTargetTile(Actor actor, GameContext gameContext, Vector2i targetTile) {
        requireNonNull(actor);
        requireNonNull(gameContext);

        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (targetTile != null) {
            worldMovement.setTargetTile(targetTile);
            navigateTowardsTarget(actor, gameContext);
            tryMovingOrTeleporting(actor, gameContext);
        }
    }

    /**
     * Tries moving or teleporting through the current level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    public void tryMovingOrTeleporting(Actor actor, GameContext gameContext) {
        requireNonNull(actor);
        requireNonNull(gameContext);

        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
        final WorldMovementPolicy worldMovementPolicy = actor.assertComponent(WorldMovementPolicy.class);
        final GameLevel level = gameContext.assertLevel();

        worldMovement.info.clear();
        if (worldMovement.canTeleport()) {
            worldMovement.info.teleported = tryTeleporting(gameContext, actor, level.worldMap().terrainLayer());
            if (worldMovement.info.teleported) {
                return;
            }
        }
        if (worldMovement.isTurnBackRequested() && worldMovementPolicy.canTurnBack(actor)) {
            setWishDir(actor, worldMovement.moveDir().opposite());
            Logger.trace("{}: turned back at tile {}", actor.name(), computeTile(actor));
            worldMovement.clearTurnBackRequested();
        }
        tryMovingTowards(actor, gameContext, computeTile(actor), worldMovement.wishDir());
        if (worldMovement.info.moved) {
            setMoveDir(actor, worldMovement.wishDir());
        } else {
            tryMovingTowards(actor, gameContext, computeTile(actor), worldMovement.moveDir());
        }
    }

    private boolean tryTeleporting(GameContext gameContext, Actor actor, TerrainLayer terrain) {
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);

        if (worldMovement.moveDir().isHorizontal()) {
            return terrain.horizontalPortals().stream()
                .filter(portal -> portal.tileY() == computeTile(actor).y())
                .findFirst()
                .map(portal -> portal.tryTeleporting(gameContext, actor))
                .orElse(false);
        }
        return false; // no vertical teleporting yet
    }

    private void tryMovingTowards(Actor actor, GameContext gameContext, Vector2i tileBeforeMoving, Direction dir) {
        final Movement movement = actor.movement();
        final WorldMovement worldMovement = actor.assertComponent(WorldMovement.class);
        final WorldMovementPolicy worldMovementPolicy = actor.assertComponent(WorldMovementPolicy.class);

        final MovementSystem movementSystem = GameContext.SYSTEMS.movementSystem;

        final Vector2f newVelocity = dir.vector().scaled(movement.computeSpeed());
        final Vector2f touchPosition = computeCenter(actor).plus(dir.vector().scaled((float) WorldMap.HTS)).plus(newVelocity);
        final Vector2i touchedTile = WorldMap.computeTileAt(touchPosition);
        final boolean turn = dir.vector().isOrthogonalTo(worldMovement.moveDir().vector());

        final GameLevel level = gameContext.assertLevel();
        if (!worldMovementPolicy.canAccessTile(gameContext, actor, touchedTile)) {
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

        if (turn && worldMovement.corneringSpeedDelta != 0) {
            final Vector2f cornerVelocity = newVelocity.plus(dir.vector().scaled(worldMovement.corneringSpeedDelta));
            Logger.trace("{} velocity around corner: {}", actor.name(), cornerVelocity.length());
            movement.setVelocity(cornerVelocity.x(), cornerVelocity.y());
            movementSystem.moveAccelerated(actor);
            // Reset velocity after moving
            movement.setVelocity(newVelocity.x(), newVelocity.y());
        } else {
            movement.setVelocity(newVelocity.x(), newVelocity.y());
            movementSystem.moveAccelerated(actor);
        }

        final Vector2i tileAfterMoving = computeTile(actor);
        worldMovement.setNewTileEntered(!tileBeforeMoving.equals(tileAfterMoving));

        worldMovement.info.moved = true;

        final TerrainLayer terrainLayer = level.worldMap().terrainLayer();

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
