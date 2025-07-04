/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.Portal;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.Direction.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all actors which know how to move through a level's world.
 *
 * @author Armin Reichert
 */
public abstract class MovingActor extends Actor {

    protected final MoveResult moveInfo = new MoveResult();

    private final ObjectProperty<Direction> moveDirProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Direction> wishDirProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Vector2i> targetTileProperty = new SimpleObjectProperty<>();

    protected boolean newTileEntered;
    protected boolean gotReverseCommand;
    protected boolean canTeleport;
    protected float corneringSpeedUp;

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
                ", gotReverseCommand=" + gotReverseCommand +
                ", canTeleport=" + canTeleport +
                ", corneringSpeedUp" + corneringSpeedUp +
                '}';
    }

    public void reset() {
        super.reset();
        moveInfo.clear();
        setMoveAndWishDir(RIGHT); // updates velocity vector!
        setTargetTile(null);
        newTileEntered = true;
        gotReverseCommand = false;
        canTeleport = true;
    }

    public MoveResult moveInfo() {
        return moveInfo;
    }

    /**
     * @return readable name, used for UI and logging
     */
    public abstract String name();

    /**
     * @return {@code true} if this actor can reverse ist direction in its current state
     */
    public abstract boolean canReverse();

    /**
     * @param level game level
     * @param tile some tile inside or outside the world
     * @return if this actor can access the given tile
     */
    public abstract boolean canAccessTile(GameLevel level, Vector2i tile);

    public ObjectProperty<Vector2i> targetTileProperty() {
        return targetTileProperty;
    }

    /**
     * Sets the tile this actor tries to reach (can be an unreachable tile or <code>null</code>).
     *
     * @param tile some tile or <code>null</code>
     */
    public void setTargetTile(Vector2i tile) {
        targetTileProperty.set(tile);
    }

    /**
     * @return current target tile. Can be null, an inaccessible tile or a tile outside the world.
     */
    public Vector2i targetTile() { return targetTileProperty.get(); }

    /**
     * @return (Optional) target tile. Can be inaccessible or outside the world.
     */
    public Optional<Vector2i> optTargetTile() {
        return Optional.ofNullable(targetTileProperty.get());
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

    public ObjectProperty<Direction> moveDirProperty() {
        return moveDirProperty;
    }

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction (must not be null)
     */
    public void setMoveDir(Direction dir) {
        moveDirProperty.set(requireNonNull(dir));
        setVelocity(dir.vector().scaled(velocity().length()));
    }

    /**
     * @return The current move direction.
     */
    public Direction moveDir() {
        return moveDirProperty.get();
    }

    public ObjectProperty<Direction> wishDirProperty() {
        return wishDirProperty;
    }

    /**
     * Sets the wish direction.
     *
     * @param dir the wish direction (must not be null)
     */
    public void setWishDir(Direction dir) {
        wishDirProperty.set(requireNonNull(dir));
    }

    /**
     * @return The wish direction. Will be taken as soon as possible.
     */
    public Direction wishDir() {
        return wishDirProperty.get();
    }

    /**
     * Sets both directions at once.
     *
     * @param dir the new wish and move direction
     */
    public void setMoveAndWishDir(Direction dir) {
        setWishDir(dir);
        setMoveDir(dir);
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
    public void reverseAtNextOccasion() {
        gotReverseCommand = true;
        Logger.debug("Reverse! {}", this);
    }

    public boolean gotReverseCommand() {
        return gotReverseCommand;
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

    public void navigateTowardsTarget(GameLevel level) {
        requireNonNull(level);
        if (!newTileEntered && moveInfo.moved || targetTileProperty.get() == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }
        final Vector2i currentTile = tile();
        if (level.isTileInPortalSpace(currentTile)) {
            return;
        }
        Direction candidateDir = null;
        double minDistToTarget = Double.MAX_VALUE;
        // Order in which directions are selected when navigation decision is met.
        for (Direction dir : List.of(UP, LEFT, DOWN, RIGHT)) {
            if (dir == moveDir().opposite()) {
                continue; // reversing the move direction is not allowed  (except to get out of dead-ends, see below)
            }
            final Vector2i neighborTile = currentTile.plus(dir.vector());
            if (canAccessTile(level, neighborTile)) {
                double dist = neighborTile.euclideanDist(targetTileProperty.get());
                if (dist < minDistToTarget) {
                    minDistToTarget = dist;
                    candidateDir = dir;
                }
            }
        }
        // if not directory could be determined, reverse move direction (leave dead-end)
        setWishDir(candidateDir != null ? candidateDir : moveDir().opposite());
    }

    /**
     * Lets an actor move towards the given target tile.
     *
     * @param level game level
     * @param targetTile target tile this actor tries to reach
     */
    public void tryMovingTowardsTargetTile(GameLevel level, Vector2i targetTile) {
        setTargetTile(targetTile);
        navigateTowardsTarget(level);
        tryMoving(level);
    }

    private void tryTeleport(Vector2i currentTile, Portal portal) {
        Vector2f oldPosition = position();
        if (currentTile.y() == portal.leftTunnelEnd().y() && oldPosition.x() < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            placeAtTile(portal.rightTunnelEnd());
            moveInfo.teleported = true;
            moveInfo.log(String.format("%s: Teleported from %s to %s", name(), oldPosition,position()));
        } else if (currentTile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            placeAtTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            moveInfo.teleported = true;
            moveInfo.log(String.format("%s: Teleported from %s to %s", name(), oldPosition,position()));
        }
    }

    /**
     * Tries moving through the level's world.
     * <p>
     * First checks if the actor can be teleported, then if the actor can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     *
     * @param level the game level
     */
    public void tryMoving(GameLevel level) {
        final Vector2i currentTile = tile();
        moveInfo.clear();
        if (canTeleport) {
            for (Portal portal : level.portals()) {
                tryTeleport(currentTile, portal);
                if (moveInfo.teleported) {
                    return;
                }
            }
        }
        if (!moveInfo.teleported) {
            if (gotReverseCommand && canReverse()) {
                setWishDir(moveDir().opposite());
                Logger.trace("{}: turned around at tile {}", name(), tile());
                gotReverseCommand = false;
            }
            tryMovingTowards(level, currentTile, wishDir());
            if (moveInfo.moved) {
                setMoveDir(wishDir());
            } else {
                tryMovingTowards(level, currentTile, moveDir());
            }
        }
    }

    private void tryMovingTowards(GameLevel level, Vector2i tileBeforeMoving, Direction dir) {
        final Vector2f newVelocity = dir.vector().scaled(velocity().length());
        final Vector2f touchPosition = center().plus(dir.vector().scaled((float) HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);
        final boolean turn = dir.vector().isOrthogonalTo(moveDir().vector());

        if (!canAccessTile(level, touchedTile)) {
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
        moveInfo.tunnelEntered = level.isTunnel(tileAfterMoving) && !level.isTunnel(tileBeforeMoving) && !level.isTileInPortalSpace(tileBeforeMoving);
        moveInfo.tunnelLeft = !level.isTunnel(tileAfterMoving) && level.isTunnel(tileBeforeMoving)  && !level.isTileInPortalSpace(tileAfterMoving);

        moveInfo.log(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));
        if (moveInfo.tunnelEntered) { Logger.trace("{} entered tunnel", name()); }
        if (moveInfo.tunnelLeft)    { Logger.trace("{} left tunnel", name()); }
    }
}