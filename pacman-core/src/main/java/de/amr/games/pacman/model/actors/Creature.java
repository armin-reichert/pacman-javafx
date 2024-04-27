/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.Portal;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Direction.*;
import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class for all creatures which can move through a level's world.
 *
 * @author Armin Reichert
 */
public abstract class Creature extends Entity {

    public final MoveResult moveResult = new MoveResult();

    private Direction moveDir;
    private Direction wishDir;
    private Vector2i targetTile;
    private float baseSpeed;

    protected boolean newTileEntered;
    protected boolean gotReverseCommand;
    protected boolean canTeleport;
    protected float corneringSpeedUp;



    public void reset() {
        super.reset();
        moveResult.clear();
        setMoveAndWishDir(RIGHT); // updates velocity vector!
        targetTile = null;
        newTileEntered = true;
        gotReverseCommand = false;
        canTeleport = true;
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
    public abstract boolean canAccessTile(Vector2i tile, World world);

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
     * @param dir the new move direction
     */
    public void setMoveDir(Direction dir) {
        checkDirectionNotNull(dir);
        if (moveDir != dir) {
            moveDir = dir;
            setVelocity(moveDir.vector().toFloatVec().scaled(velocity().length()));
            Logger.trace("{}: New moveDir: {}. {}", name(), moveDir, this);
        }
    }

    /**
     * @return The current move direction.
     */
    public Direction moveDir() {
        return moveDir;
    }

    /**
     * Sets the wish direction and updates the velocity vector.
     *
     * @param dir the new wish direction
     */
    public void setWishDir(Direction dir) {
        checkDirectionNotNull(dir);
        if (wishDir != dir) {
            wishDir = dir;
            Logger.trace("{}: New wishDir: {}. {}", name(), wishDir, this);
        }
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
     * In case the creature looks UP, additional {@code numTiles} tiles are added towards LEFT.
     * This simulates an overflow bug in the original Arcade games.
     */
    public Vector2i tilesAheadWithOverflowBug(int numTiles) {
        Vector2i ahead = tile().plus(moveDir.vector().scaled(numTiles));
        return moveDir == Direction.UP ? ahead.minus(numTiles, 0) : ahead;
    }

    /**
     * Signals that this creature should reverse its move direction as soon as possible.
     */
    public void reverseAsSoonAsPossible() {
        gotReverseCommand = true;
        Logger.info("Reverse! {}", this);
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
        setVelocity(pixelSpeed == 0 ? Vector2f.ZERO : moveDir.vector().toFloatVec().scaled(pixelSpeed));
    }

    public boolean isNewTileEntered() {
        return newTileEntered;
    }

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

    /** Order in which directions are selected when navigation decision is met. */
    Direction[] DIRECTION_PRIORITY = {UP, LEFT, DOWN, RIGHT};

    /**
     * Implements the rules by which a creature (ghost) decides how to reach its current target tile.
     *
     * @param world the world/maze
     * @return optional direction the creature takes to reach its current target tile
     */
    public Optional<Direction> computeTargetDirection(World world) {
        if (targetTile().isEmpty()) {
            return Optional.empty();
        }
        final var currentTile = tile();
        Direction targetDir = null;
        float minDistance = Float.MAX_VALUE;
        for (var dir : DIRECTION_PRIORITY) {
            if (dir == moveDir().opposite()) {
                continue; // reversing the move direction is not allowed
            }
            final var neighborTile = currentTile.plus(dir.vector());
            if (canAccessTile(neighborTile, world)) {
                final float distance = neighborTile.euclideanDistance(targetTile().get());
                if (distance < minDistance) {
                    minDistance = distance;
                    targetDir = dir;
                }
            }
        }
        return Optional.ofNullable(targetDir);
    }

    /**
     * Sets the new wish direction for reaching the target tile.
     *
     * @param world the world/maze
     */
    public void navigateTowardsTarget(World world) {
        if (targetTile().isEmpty()) {
            return;
        }
        if (!newTileEntered && moveResult.moved) {
            return; // we don't need no navigation, dim dit diddit diddit...
        }
        if (world.belongsToPortal(tile())) {
            return; // inside portal, no navigation happens
        }
        computeTargetDirection(world).ifPresent(this::setWishDir);
    }

    /**
     * Lets a creature follow the given target tile.
     *
     * @param world the world/maze
     * @param targetTile the target tile e.g. Pac-Man's current tile
     * @param relSpeed the relative speed (in percentage of base speed)
     */
    public void followTarget(World world, Vector2i targetTile, byte relSpeed) {
        setSpeedPct(relSpeed);
        setTargetTile(targetTile);
        navigateTowardsTarget(world);
        tryMoving(world);
    }

    /**
     * Tries moving through the game world.
     * <p>
     * First checks if the creature can teleport, then if the creature can move to its wish direction. If this is not
     * possible, it keeps moving to its current move direction.
     *
     * @param world the world/maze
     */
    public void tryMoving(World world) {
        moveResult.clear();
        tryTeleport(world.portals());
        if (!moveResult.teleported) {
            if (gotReverseCommand && canReverse()) {
                setWishDir(moveDir().opposite());
                Logger.info("{}: turned around at tile {}", name(), tile());
                gotReverseCommand = false;
            }
            tryMoving(world, wishDir());
            if (moveResult.moved) {
                setMoveDir(wishDir());
            } else {
                tryMoving(world, moveDir());
            }
        }
        if (moveResult.teleported || moveResult.moved) {
            Logger.trace("{}: {} {} {}", name(), moveResult, moveResult.summary(), this);
        }
    }

    /**
     * Tries to teleport a creature through a portal.
     *
     * @param portals list of available portals
     */
    public void tryTeleport(List<Portal> portals) {
        if (canTeleport) {
            for (var portal : portals) {
                tryTeleport(portal);
                if (moveResult.teleported) {
                    return;
                }
            }
        }
    }

    /**
     * Tries to teleport a creature through a portal.
     *
     * @param portal a portal
     */
    public void tryTeleport(Portal portal) {
        var tile = tile();
        var oldX = posX;
        var oldY = posY;
        if (tile.y() == portal.leftTunnelEnd().y() && posX < portal.leftTunnelEnd().x() - portal.depth() * TS) {
            centerOverTile(portal.rightTunnelEnd());
            moveResult.teleported = true;
            moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name(), oldX, oldY, posX, posY));
        } else if (tile.equals(portal.rightTunnelEnd().plus(portal.depth(), 0))) {
            centerOverTile(portal.leftTunnelEnd().minus(portal.depth(), 0));
            moveResult.teleported = true;
            moveResult.addMessage(String.format("%s: Teleported from (%.2f,%.2f) to (%.2f,%.2f)",
                name(), oldX, oldY, posX, posY));
        }
    }

    /**
     * Tries to move a creature towards the given directory. Handles collisions with walls and moving around corners.
     *
     * @param world the world/maze
     * @param dir the direction to move
     */
    public void tryMoving(World world, Direction dir) {
        final Vector2i tileBeforeMove = tile();
        final Vector2f dirVector = dir.vector().toFloatVec();
        final Vector2f newVelocity = dirVector.scaled(velocity().length());
        final Vector2f touchPosition = center().plus(dirVector.scaled(HTS)).plus(newVelocity);
        final Vector2i touchedTile = tileAt(touchPosition);
        final boolean isTurn = !dir.sameOrientation(moveDir());

        if (!canAccessTile(touchedTile, world)) {
            if (!isTurn) {
                centerOverTile(tile()); // adjust over tile (would move forward against wall)
            }
            moveResult.addMessage(String.format("Cannot move %s into tile %s", dir, touchedTile));
            return;
        }

        if (isTurn) {
            float offset = dir.isHorizontal() ? offset().y() : offset().x();
            boolean atTurnPosition = Math.abs(offset) <= 1; // TODO <= pixel-speed?
            if (atTurnPosition) {
                centerOverTile(tile()); // adjust over tile (starts moving around corner)
            } else {
                moveResult.addMessage(String.format("Wants to take corner towards %s but not at turn position", dir));
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
        moveResult.moved = true;
        moveResult.tunnelEntered = world.isTunnel(currentTile)
            && !world.isTunnel(tileBeforeMove)
            && !world.belongsToPortal(tileBeforeMove);
        moveResult.tunnelLeft = !world.isTunnel(currentTile)
            && world.isTunnel(tileBeforeMove)
            && !world.belongsToPortal(currentTile);

        moveResult.addMessage(String.format("%5s (%.2f pixels)", dir, newVelocity.length()));

        if (moveResult.tunnelEntered) {
            Logger.trace("{} entered tunnel", name());
        }
        if (moveResult.tunnelLeft) {
            Logger.trace("{} left tunnel", name());
        }
    }
}