/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.Portal;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class for all creatures which can move through a level's world.
 *
 * @author Armin Reichert
 */
public abstract class Creature extends Entity {

    /** Order in which directions are selected when navigation decision is met. */
    Direction[] DIRECTION_PRIORITY = {UP, LEFT, DOWN, RIGHT};

    protected final MoveResult moveInfo = new MoveResult();

    protected GameWorld world;

    protected Direction moveDir;
    protected Direction wishDir;
    protected Vector2i targetTile;
    protected float baseSpeed;

    protected boolean newTileEntered;
    protected boolean gotReverseCommand;
    protected boolean canTeleport;
    protected float corneringSpeedUp;

    @Override
    public String toString() {
        return "Creature{" +
            "posX=" + posX +
            ", posY=" + posY +
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
     * @param pixelsPerTick number of pixels the creature moves in one tick
     */
    public void setBaseSpeed(float pixelsPerTick) {
        baseSpeed = pixelsPerTick;
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
     * @param tile some tile inside or outside the world
     * @return if this creature can access the given tile
     */
    public abstract boolean canAccessTile(Vector2i tile);

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

    public void setWorld(GameWorld world) {
        this.world = checkNotNull(world);
    }

    public GameWorld world() {
        return world;
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
        moveDir = checkDirectionNotNull(dir);
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
        wishDir = checkDirectionNotNull(dir);
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
     * @param overflowBug if {@code true} the Arcade game overflow bug is simulated
     * @return the tile located the given number of tiles towards the current move direction of the creature.
     *          Overflow bug: In case the creature looks UP, additional {@code numTiles} tiles are added towards LEFT.
     */
    public Vector2i tilesAhead(int numTiles, boolean overflowBug) {
        Vector2i currentTile = tile();
        int x = currentTile.x() + moveDir.vector().x() * numTiles;
        int y = currentTile.y() + moveDir.vector().y() * numTiles;
        if (overflowBug && moveDir == UP) {
            x -= numTiles;
        }
        return new Vector2i(x, y);
    }

    /**
     * Signals that this creature should reverse its move direction as soon as possible.
     */
    public void reverseAsSoonAsPossible() {
        gotReverseCommand = true;
        Logger.debug("Reverse! {}", this);
    }

    public boolean gotReverseCommand() {
        return gotReverseCommand;
    }

    /**
     * Sets the speed in percent of the base speed (1.25 pixels/second).
     *
     * @param percentage percentage of base speed
     */
    public void setSpeedPct(byte percentage) {
        if (percentage < 0) {
            throw new IllegalArgumentException("Negative speed percentage: " + percentage);
        }
        setSpeed((float) 0.01 * percentage * baseSpeed);
    }

    /**
     * Sets the absolute speed and updates the velocity vector.
     *
     * @param pixelSpeed speed in pixels
     */
    public void setSpeed(float pixelSpeed) {
        if (pixelSpeed < 0) {
            throw new IllegalArgumentException("Negative pixel speed: " + pixelSpeed);
        }
        setVelocity(pixelSpeed == 0 ? Vector2f.ZERO : moveDir.vector().toVector2f().scaled(pixelSpeed));
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    private Optional<Direction> computeTargetDirection(Vector2i currentTile, Vector2i targetTile) {
        Direction targetDir = null;
        double minDistToTarget = Double.MAX_VALUE;
        for (Direction dir : DIRECTION_PRIORITY) {
            if (dir == moveDir.opposite()) {
                continue; // reversing the move direction is not allowed
            }
            Vector2i neighborTile = currentTile.plus(dir.vector());
            if (canAccessTile(neighborTile)) {
                double d = neighborTile.euclideanDistance(targetTile);
                if (d < minDistToTarget) {
                    minDistToTarget = d;
                    targetDir = dir;
                }
            }
        }
        return Optional.ofNullable(targetDir);
    }

    /**
     * Sets the new wish direction for reaching the target tile.
     */
    public void navigateTowardsTarget() {
        if (!newTileEntered && moveInfo.moved || targetTile == null) {
            return; // we don't need no navigation, dim dit didit didit...
        }
        Vector2i currentTile = tile();
        if (!world.isPortalAt(currentTile)) {
            computeTargetDirection(currentTile, targetTile).ifPresent(this::setWishDir);
        }
    }

    /**
     * Lets a creature follow the given target tile.
     *
     * @param targetTile target tile this creature tries to reach
     * @param speedPct relative speed (in percentage of base speed)
     */
    public void followTarget(Vector2i targetTile, byte speedPct) {
        setSpeedPct(speedPct);
        setTargetTile(targetTile);
        navigateTowardsTarget();
        tryMoving();
    }

    /**
     * Tries moving through the game world.
     * <p>
     * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     */
    public void tryMoving() {
        moveInfo.clear();
        tryTeleport();
        if (!moveInfo.teleported) {
            if (gotReverseCommand && canReverse()) {
                setWishDir(moveDir.opposite());
                Logger.trace("{}: turned around at tile {}", name(), tile());
                gotReverseCommand = false;
            }
            tryMoving(wishDir);
            if (moveInfo.moved) {
                setMoveDir(wishDir);
            } else {
                tryMoving(moveDir);
            }
        }
        if (moveInfo.teleported || moveInfo.moved) {
            Logger.trace("{}: {} {} {}", name(), moveInfo, String.join(", ", moveInfo.infos), this);
        }
    }

    private void tryTeleport() {
        if (!canTeleport) {
            return;
        }
        Vector2i currentTile = tile();
        for (Portal portal : world.portals().toList()) {
            tryTeleport(currentTile, portal);
            if (moveInfo.teleported) {
                return;
            }
        }
    }

    private void tryTeleport(Vector2i currentTile, Portal portal) {
        var oldX = posX;
        var oldY = posY;
        if (currentTile.y() == portal.leftTunnelEnd().y() && posX < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            centerOverTile(portal.rightTunnelEnd());
            moveInfo.teleported = true;
            moveInfo.log(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name(), oldX, oldY, posX, posY));
        } else if (currentTile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            centerOverTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            moveInfo.teleported = true;
            moveInfo.log(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name(), oldX, oldY, posX, posY));
        }
    }

    /**
     * Tries to move a creature towards the given direction. Handles collisions with walls and cornering.
     *
     * @param dir the direction to move
     */
    private void tryMoving(Direction dir) {
        final Vector2i tileBeforeMove = tile();
        final Vector2f dirVector = dir.vector().toVector2f();
        final Vector2f newVelocity = dirVector.scaled(velocity().length());
        final Vector2f touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);
        final boolean isTurn = !dir.sameOrientation(moveDir);

        if (!canAccessTile(touchedTile)) {
            if (!isTurn) {
                centerOverTile(tile()); // adjust over tile (would move forward against wall)
            }
            moveInfo.log(String.format("Cannot move %s into tile %s", dir, touchedTile));
            return;
        }

        if (isTurn) {
            float offset = dir.isHorizontal() ? offset().y() : offset().x();
            float speed = velocity().length();
            boolean atTurnPosition = Math.abs(offset) <= 0.5 * speed;
            if (atTurnPosition) {
                centerOverTile(tile()); // adjust over tile (starts moving around corner)
            } else {
                moveInfo.log(String.format("Wants to take corner towards %s but not at turn position", dir));
                return;
            }
        }

        if (isTurn && corneringSpeedUp > 0) {
            setVelocity(newVelocity.plus(dirVector.scaled(corneringSpeedUp)));
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
        moveInfo.tunnelEntered = world.isTunnel(currentTile)
            && !world.isTunnel(tileBeforeMove)
            && !world.isPortalAt(tileBeforeMove);
        moveInfo.tunnelLeft = !world.isTunnel(currentTile)
            && world.isTunnel(tileBeforeMove)
            && !world.isPortalAt(currentTile);

        moveInfo.log(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));

        if (moveInfo.tunnelEntered) {
            Logger.trace("{} entered tunnel", name());
        }
        if (moveInfo.tunnelLeft) {
            Logger.trace("{} left tunnel", name());
        }
    }
}