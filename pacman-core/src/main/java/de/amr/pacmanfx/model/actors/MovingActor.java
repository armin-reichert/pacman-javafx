/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameLevel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.Direction.RIGHT;
import static de.amr.pacmanfx.lib.Direction.UP;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all actors which know how to move through a level's world.
 */
public abstract class MovingActor extends Actor {

    public static final Direction DEFAULT_MOVE_DIR = RIGHT;
    public static final Direction DEFAULT_WISH_DIR = RIGHT;
    public static final Vector2i DEFAULT_TARGET_TILE = null;
    public static final boolean DEFAULT_CAN_TELEPORT = true;

    /** Order in which directions are selected when navigation decision is met. */
    public static final List<Direction> NAVIGATION_ORDER = List.of(Direction.UP, Direction.LEFT, Direction.DOWN, Direction.RIGHT);

    protected final MoveInfo moveInfo = new MoveInfo();

    private final String name;

    protected ObjectProperty<Direction> moveDir;
    protected ObjectProperty<Direction> wishDir;
    protected ObjectProperty<Vector2i> targetTile;

    protected boolean newTileEntered;
    protected boolean turnBackRequested;
    protected boolean canTeleport = DEFAULT_CAN_TELEPORT;
    protected boolean teleporting;

    //TODO this is just a primitive way to provide cornering speed differences
    protected float corneringSpeedUp;

    protected MovingActor(String name) {
        this.name = requireNonNull(name);
    }

    /**
     * Called on every tick of the game clock.
     *
     * @param gameContext the game context
     */
    public abstract void tick(GameContext gameContext);

    /**
     * @return readable name, used in UI and logging
     */
    public final String name() {
        return name;
    }

    /**
     * @param gameLevel the game level we are in (not null)
     * @param tile some tile inside or outside the world
     * @return if this actor can access the given tile in its game context
     */
    public abstract boolean canAccessTile(GameLevel gameLevel, Vector2i tile);

    /**
     * @return {@code true} if this actor can reverse ist direction in its current state
     */
    public abstract boolean canTurnBack();

    @Override
    public String toString() {
        return "WorldMovingActor{" +
            "visible=" + isVisible() +
            ", position=" + position() +
            ", velocity=" + velocity() +
            ", acceleration=" + acceleration() +
            ", moveDir=" + moveDir() +
            ", wishDir=" + wishDir() +
            ", targetTile=" + targetTile() +
            ", newTileEntered=" + newTileEntered +
            ", turnBackRequested=" + turnBackRequested +
            ", canTeleport=" + canTeleport +
            ", teleporting=" + teleporting +
            ", corneringSpeedUp" + corneringSpeedUp +
            '}';
    }

    public void reset() {
        super.reset();
        moveInfo.clear();
        if (moveDir != null) {
            setMoveDir(DEFAULT_MOVE_DIR);  // updates velocity vector!
        }
        if (wishDir != null) {
            setWishDir(DEFAULT_WISH_DIR);
        }
        if (targetTile != null) {
            setTargetTile(DEFAULT_TARGET_TILE);
        }
        canTeleport = DEFAULT_CAN_TELEPORT;
        teleporting = false;
        newTileEntered = true;
        turnBackRequested = false;
    }

    public MoveInfo moveInfo() {
        return moveInfo;
    }

    public final ObjectProperty<Vector2i> targetTileProperty() {
        if (targetTile == null) {
            targetTile = new SimpleObjectProperty<>(DEFAULT_TARGET_TILE);
        }
        return targetTile;
    }

    /**
     * Sets the tile this actor tries to reach (can be an unreachable tile or <code>null</code>).
     *
     * @param tile some tile or <code>null</code>
     */
    public void setTargetTile(Vector2i tile) {
        targetTileProperty().set(tile);
    }

    /**
     * @return current target tile. Can be null, an inaccessible tile or a tile outside the world.
     */
    public Vector2i targetTile() {
        return targetTile != null ? targetTileProperty().get() : DEFAULT_TARGET_TILE;
    }

    /**
     * @return (Optional) target tile. Can be inaccessible or outside the world.
     */
    public Optional<Vector2i> optTargetTile() {
        return Optional.ofNullable(targetTile());
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
    public void placeAtTile(int tx, int ty, float ox, float oy) {
        var prevTile = tile();
        setPosition(tx * TS + ox, ty * TS + oy);
        newTileEntered = !tile().equals(prevTile);
    }

    /**
     * Places this actor exactly at the given tile coordinate. Updates the <code>newTileEntered</code> state.
     *
     * @param tx tile x-coordinate (grid column)
     * @param ty tile y-coordinate (grid row)
     */
    public void placeAtTile(int tx, int ty) { placeAtTile(tx, ty, 0, 0); }

    /**
     * Places this actor centered over the given tile.
     *
     * @param tile tile where actor is placed
     */
    public void placeAtTile(Vector2i tile) {
        placeAtTile(tile.x(), tile.y());
    }

    public final ObjectProperty<Direction> moveDirProperty() {
        if (moveDir == null) {
            moveDir = new SimpleObjectProperty<>(DEFAULT_MOVE_DIR);
        }
        return moveDir;
    }

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction (must not be null)
     */
    public void setMoveDir(Direction dir) {
        requireNonNull(dir);
        if (moveDir == null && dir.equals(DEFAULT_MOVE_DIR)) return;
        moveDirProperty().set(dir);
        double speed = velocity().length();
        setVelocity(speed == 0 ? Vector2f.ZERO : dir.vector().scaled(speed));
    }

    /**
     * @return The current move direction.
     */
    public Direction moveDir() {
        return moveDir != null ? moveDirProperty().get() : DEFAULT_MOVE_DIR;
    }

    public final ObjectProperty<Direction> wishDirProperty() {
        if (wishDir == null) {
            wishDir = new SimpleObjectProperty<>(DEFAULT_WISH_DIR);
        }
        return wishDir;
    }

    /**
     * Sets the wish direction.
     *
     * @param dir the wish direction (must not be null)
     */
    public void setWishDir(Direction dir) {
        requireNonNull(dir);
        if (wishDir == null && dir.equals(DEFAULT_WISH_DIR)) return;
        wishDirProperty().set(dir);
    }

    /**
     * @return The wish direction. Will be taken as soon as possible.
     */
    public Direction wishDir() {
        return wishDir != null ? wishDirProperty().get() : DEFAULT_WISH_DIR;
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     */
    public Vector2i tilesAhead(int numTiles) {
        return tile().plus(moveDir().vector().scaled(numTiles));
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the actor.
     *          Overflow bug: In case the actor looks UP, additional {@code numTiles} tiles are added towards LEFT.
     */
    public Vector2i tilesAheadWithOverflowBug(int numTiles) {
        Vector2i ahead = tilesAhead(numTiles);
        if (moveDir() == UP) {
            ahead = ahead.minus(numTiles, 0);
        }
        return ahead;
    }

    /**
     * Signals that this actor should reverse its move direction as soon as possible.
     */
    public void requestTurnBack() {
        turnBackRequested = true;
        Logger.debug("Turn back ASAP! {}", this);
    }

    /**
     * @return {@code true} if the ghost should revert its direction at the next occasion
     */
    public boolean turnBackRequested() {
        return turnBackRequested;
    }

    /**
     * Sets the absolute speed and updates the velocity vector.
     *
     * @param speed speed in pixels/tick
     */
    public void setSpeed(float speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("Speed must not be negative but is: " + speed);
        }
        setVelocity(speed == 0 ? Vector2f.ZERO : moveDir().vector().scaled(speed));
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    public void navigateTowardsTarget(GameLevel gameLevel) {
        requireNonNull(gameLevel);

        if (!newTileEntered && moveInfo.moved || targetTile() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }

        final Vector2i currentTile = tile();
        if (gameLevel.worldMap().terrainLayer().isTileInPortalSpace(currentTile)) {
            return;
        }
        Direction candidateDir = null;
        double minDistToTarget = Double.MAX_VALUE;
        for (Direction dir : NAVIGATION_ORDER) {
            if (dir == moveDir().opposite()) {
                continue; // reversing the move direction is not allowed  (except to get out of dead-ends, see below)
            }
            final Vector2i neighborTile = currentTile.plus(dir.vector());
            if (canAccessTile(gameLevel, neighborTile)) {
                double dist = neighborTile.euclideanDist(targetTile());
                if (dist < minDistToTarget) {
                    minDistToTarget = dist;
                    candidateDir = dir;
                }
            }
        }
        // if no direction could be determined, reverse (exit from dead-end)
        setWishDir(candidateDir != null ? candidateDir : moveDir().opposite());
    }

    /**
     * Lets an actor move towards the given target tile.
     *
     * @param gameLevel the game level we are in
     * @param targetTile target tile this actor tries to reach
     */
    public void tryMovingTowardsTargetTile(GameLevel gameLevel, Vector2i targetTile) {
        requireNonNull(gameLevel);
        if (targetTile != null) {
            setTargetTile(targetTile);
            navigateTowardsTarget(gameLevel);
            moveThroughThisCruelWorld(gameLevel);
        }
    }

    /**
     * Tries moving through the current level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     *
     * @param gameLevel the game level we are in
     */
    public void moveThroughThisCruelWorld(GameLevel gameLevel) {
        requireNonNull(gameLevel);
        moveInfo.clear();
        if (canTeleport) {
            boolean teleported = tryHorizontalTeleport(gameLevel.worldMap().terrainLayer());
            if (teleported) {
                return;
            }
        }
        if (turnBackRequested && canTurnBack()) {
            setWishDir(moveDir().opposite());
            Logger.trace("{}: turned back at tile {}", name(), tile());
            turnBackRequested = false;
        }
        tryMovingTowards(gameLevel, tile(), wishDir());
        if (moveInfo.moved) {
            setMoveDir(wishDir());
        } else {
            tryMovingTowards(gameLevel, tile(), moveDir());
        }
    }

    private boolean tryHorizontalTeleport(TerrainLayer terrain) {
        if (moveDir().isVertical()) {
            return false;
        }
        return terrain.horizontalPortals().stream()
            .filter(portal -> portal.leftBorderEntryTile().y() == tile().y())
            .findFirst()
            .map(portal -> portal.tryTeleporting(this))
            .orElse(false);
    }

    private void tryMovingTowards(GameLevel gameLevel, Vector2i tileBeforeMoving, Direction dir) {
        final Vector2f newVelocity = dir.vector().scaled(velocity().length());
        final Vector2f touchPosition = center().plus(dir.vector().scaled((float) HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);
        final boolean turn = dir.vector().isOrthogonalTo(moveDir().vector());

        if (!canAccessTile(gameLevel, touchedTile)) {
            if (!turn) {
                placeAtTile(tile()); // adjust over tile (would move forward against wall)
            }
            moveInfo.log(String.format("Cannot move %s into tile %s", dir, touchedTile));
            return;
        }

        if (turn) {
            float offset = dir.isHorizontal() ? offset().y() : offset().x();
            boolean atTurnPosition = Math.abs(offset) <= 1;
            if (atTurnPosition) {
                Logger.trace("Reached turn position ({})", name());
                placeAtTile(tile()); // adjust over tile (starts moving around corner)
            } else {
                moveInfo.log(String.format("Wants to take corner towards %s but not at turn position", dir));
                return;
            }
        }

        if (turn && corneringSpeedUp != 0) {
            Vector2f cornerVelocity = newVelocity.plus(dir.vector().scaled(corneringSpeedUp));
            Logger.trace("{} velocity around corner: {}", name(), cornerVelocity.length());
            setVelocity(cornerVelocity);
            move();
            setVelocity(newVelocity);
        } else {
            setVelocity(newVelocity);
            move();
        }

        final Vector2i tileAfterMoving = tile();
        newTileEntered = !tileBeforeMoving.equals(tileAfterMoving);

        moveInfo.moved = true;
        TerrainLayer terrainLayer = gameLevel.worldMap().terrainLayer();
        moveInfo.tunnelEntered = terrainLayer.isTunnel(tileAfterMoving)
            && !terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileBeforeMoving);
        moveInfo.tunnelLeft = !terrainLayer.isTunnel(tileAfterMoving)
            && terrainLayer.isTunnel(tileBeforeMoving)
            && !terrainLayer.isTileInPortalSpace(tileAfterMoving);

        moveInfo.log(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));
        if (moveInfo.tunnelEntered) { Logger.trace("{} entered tunnel", name()); }
        if (moveInfo.tunnelLeft)    { Logger.trace("{} left tunnel", name()); }
    }
}