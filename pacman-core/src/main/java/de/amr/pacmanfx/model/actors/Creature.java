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
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.Direction.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all creatures which can move through a level's world.
 *
 * @author Armin Reichert
 */
public abstract class Creature extends Actor {

    /** Order in which directions are selected when navigation decision is met. */
    private static final Direction[] DIRECTION_PRIORITY = {UP, LEFT, DOWN, RIGHT};

    protected final MoveResult moveInfo = new MoveResult();

    protected Direction moveDir;
    protected Direction wishDir;
    protected Vector2i targetTile;

    protected boolean newTileEntered;
    protected boolean gotReverseCommand;
    protected boolean canTeleport;
    protected float corneringSpeedUp;

    @Override
    public String toString() {
        return "Creature{" +
            "posX=" + x +
            ", posY=" + y +
            ", moveDir=" + moveDir +
            ", wishDir=" + wishDir +
            ", newTileEntered=" + newTileEntered +
            ", gotReverseCommand=" + gotReverseCommand +
            '}';
    }

    public void reset() {
        super.reset();
        moveInfo.clear();
        setMoveAndWishDir(RIGHT); // updates velocity vector!
        targetTile = null;
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
     * @return {@code true} if this creature can reverse ist direction in its current state
     */
    public abstract boolean canReverse();

    /**
     * @param level game level
     * @param tile some tile inside or outside the world
     * @return if this creature can access the given tile
     */
    public abstract boolean canAccessTile(GameLevel level, Vector2i tile);

    /**
     * Sets the tile this creature tries to reach (can be an unreachable tile or <code>null</code>).
     *
     * @param tile some tile or <code>null</code>
     */
    public void setTargetTile(Vector2i tile) {
        targetTile = tile;
    }

    /**
     * @return (Optional) target tile. Can be inaccessible or outside the world.
     */
    public Optional<Vector2i> targetTile() {
        return Optional.ofNullable(targetTile);
    }

    /**
     * Places this creature at the given tile coordinate with the given tile offsets. Updates the
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
     * Places this creature centered over the given tile.
     *
     * @param tile tile where creature is placed
     */
    public void centerOverTile(Vector2i tile) {
        placeAtTile(tile.x(), tile.y(), 0, 0);
    }

    /**
     * Sets the move direction and updates the velocity vector.
     *
     * @param dir the move direction
     */
    public void setMoveDir(Direction dir) {
        moveDir = requireNonNull(dir);
        setVelocity(moveDir.vector().scaled(velocity().length()));
        Logger.trace("{}: moveDir: {}. {}", name(), moveDir, this);
    }

    /**
     * @return The current move direction.
     */
    public Direction moveDir() {
        return moveDir;
    }

    /**
     * Sets the wish direction.
     *
     * @param dir the wish direction
     */
    public void setWishDir(Direction dir) {
        wishDir = requireNonNull(dir);
        Logger.trace("{}: wishDir: {}. {}", name(), wishDir, this);
    }

    /**
     * @return The wish direction. Will be taken as soon as possible.
     */
    public Direction wishDir() {
        return wishDir;
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
     * @return the tile located the given number of tiles towards the current move direction of the creature.
     */
    public Vector2i tilesAhead(int numTiles) {
        Vector2i currentTile = tile();
        int x = currentTile.x() + moveDir.vector().x() * numTiles;
        int y = currentTile.y() + moveDir.vector().y() * numTiles;
        return new Vector2i(x, y);
    }

    /**
     * @param numTiles number of tiles
     * @return the tile located the given number of tiles towards the current move direction of the creature.
     *          Overflow bug: In case the creature looks UP, additional {@code numTiles} tiles are added towards LEFT.
     */
    public Vector2i tilesAheadWithArcadeOverflowBug(int numTiles) {
        Vector2i ahead = tilesAhead(numTiles);
        if (moveDir == UP) {
            ahead = ahead.minus(numTiles, 0);
        }
        return ahead;
    }

    /**
     * Signals that this creature should reverse its move direction as soon as possible.
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
            throw new IllegalArgumentException("Negative pixel speed: " + speed);
        }
        setVelocity(speed == 0 ? Vector2f.ZERO : moveDir.vector().scaled(speed));
    }

    public float speed() {
        return velocity().length();
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    private Optional<Direction> computeTargetDirection(GameLevel level, Vector2i currentTile, Vector2i targetTile) {
        Direction targetDir = null;
        double minDistToTarget = Double.MAX_VALUE;
        for (Direction dir : DIRECTION_PRIORITY) {
            if (dir == moveDir.opposite()) {
                continue; // reversing the move direction is not allowed
            }
            Vector2i neighborTile = currentTile.plus(dir.vector());
            if (canAccessTile(level, neighborTile)) {
                double d = neighborTile.euclideanDist(targetTile);
                if (d < minDistToTarget) {
                    minDistToTarget = d;
                    targetDir = dir;
                }
            }
        }
        if (targetDir == null) {
            targetDir = moveDir.opposite();
        }
        return Optional.ofNullable(targetDir);
    }

    /**
     * Sets the new wish direction for reaching the target tile.
     */
    public void navigateTowardsTarget(GameLevel level) {
        if (!newTileEntered && moveInfo.moved || targetTile == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }
        Vector2i currentTile = tile();
        if (!level.isPortalAt(currentTile)) {
            computeTargetDirection(level, currentTile, targetTile).ifPresent(this::setWishDir);
        }
    }

    /**
     * Lets a creature follow the given target tile.
     *
     * @param targetTile target tile this creature tries to reach
     * @param speed speed (in pixels/tick)
     */
    public void followTarget(GameLevel level, Vector2i targetTile, float speed) {
        setSpeed(speed);
        setTargetTile(targetTile);
        navigateTowardsTarget(level);
        tryMoving(level);
    }

    /**
     * Tries moving through the game world.
     * <p>
     * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    public void tryMoving(GameLevel level) {
        moveInfo.clear();
        tryTeleport(level);
        if (!moveInfo.teleported) {
            if (gotReverseCommand && canReverse()) {
                setWishDir(moveDir.opposite());
                Logger.trace("{}: turned around at tile {}", name(), tile());
                gotReverseCommand = false;
            }
            tryMoving(level, wishDir);
            if (moveInfo.moved) {
                setMoveDir(wishDir);
            } else {
                tryMoving(level, moveDir);
            }
        }
        if (moveInfo.teleported || moveInfo.moved) {
            Logger.trace("{}: {} {} {}", name(), moveInfo, String.join(", ", moveInfo.infos), this);
        }
    }

    private void tryTeleport(GameLevel level) {
        if (!canTeleport) {
            return;
        }
        Vector2i currentTile = tile();
        for (Portal portal : level.portals().toList()) {
            tryTeleport(currentTile, portal);
            if (moveInfo.teleported) {
                return;
            }
        }
    }

    private void tryTeleport(Vector2i currentTile, Portal portal) {
        var oldX = x;
        var oldY = y;
        if (currentTile.y() == portal.leftTunnelEnd().y() && x < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            centerOverTile(portal.rightTunnelEnd());
            moveInfo.teleported = true;
            moveInfo.log(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name(), oldX, oldY, x, y));
        } else if (currentTile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            centerOverTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            moveInfo.teleported = true;
            moveInfo.log(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name(), oldX, oldY, x, y));
        }
    }

    /**
     * Tries to move a creature towards the given direction. Handles collisions with walls and cornering.
     *
     * @param dir the direction to move
     */
    protected void tryMoving(GameLevel level, Direction dir) {
        final boolean isTurn = !dir.sameOrientation(moveDir);
        final Vector2i tileBeforeMove = tile();
        final Vector2f newVelocity = dir.vector().scaled(velocity().length());
        final Vector2f touchPosition = position().plus(HTS, HTS).plus(dir.vector().scaled((float) HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);

        if (!canAccessTile(level, touchedTile)) {
            if (!isTurn) {
                centerOverTile(tile()); // adjust over tile (would move forward against wall)
            }
            moveInfo.log(String.format("Cannot move %s into tile %s", dir, touchedTile));
            return;
        }

        if (isTurn) {
            float offset = dir.isHorizontal() ? offset().y() : offset().x();
            boolean atTurnPosition = Math.abs(offset) <= 1;
            if (atTurnPosition) {
                Logger.trace("Reached turn position ({})", name());
                centerOverTile(tile()); // adjust over tile (starts moving around corner)
            } else {
                moveInfo.log(String.format("Wants to take corner towards %s but not at turn position", dir));
                return;
            }
        }

        if (isTurn && corneringSpeedUp != 0) {
            setVelocity(newVelocity.plus(dir.vector().scaled(corneringSpeedUp)));
            Logger.trace("{} velocity around corner: {}", name(), velocity().length());
            move();
            setVelocity(newVelocity);
        } else {
            setVelocity(newVelocity);
            move();
        }

        Vector2i currentTile = tile();

        newTileEntered = !tileBeforeMove.equals(currentTile);
        moveInfo.moved = true;
        moveInfo.tunnelEntered = level.isTunnel(currentTile)
            && !level.isTunnel(tileBeforeMove)
            && !level.isPortalAt(tileBeforeMove);
        moveInfo.tunnelLeft = !level.isTunnel(currentTile)
            && level.isTunnel(tileBeforeMove)
            && !level.isPortalAt(currentTile);

        moveInfo.log(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));

        if (moveInfo.tunnelEntered) {
            Logger.trace("{} entered tunnel", name());
        }
        if (moveInfo.tunnelLeft) {
            Logger.trace("{} left tunnel", name());
        }
    }
}