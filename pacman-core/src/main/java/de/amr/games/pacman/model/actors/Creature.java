/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.world.World;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.lib.Direction.RIGHT;
import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class for all creatures which can move through a level's world.
 *
 * @author Armin Reichert
 */
public abstract class Creature extends Entity {

    public final String name;
    public final MoveResult moveResult = new MoveResult();

    private Direction moveDir;
    private Direction wishDir;
    private Vector2i targetTile;
    private float baseSpeed;

    public boolean newTileEntered;
    protected boolean gotReverseCommand;
    protected boolean canTeleport;
    protected float corneringSpeedUp;

    /**
     *  @param name Readable name, for display and logging purposes.
     */
    protected Creature(String name) {
        checkNotNull(name, "Name of creature must not be null");
        this.name = name;
    }

    public void reset() {
        super.reset();

        moveDir = RIGHT;
        wishDir = RIGHT;
        targetTile = null;

        moveResult.clear();
        newTileEntered = true;
        gotReverseCommand = false;
        canTeleport = true;
    }

    public void setBaseSpeed(float pixelsPerTick) {
        baseSpeed = pixelsPerTick;
    }

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
     * Places this creature at the given tile coordinate with the given tile offsets. Updates the
     * <code>newTileEntered</code> state.
     *
     * @param tile tile
     * @param ox   x-offset inside tile
     * @param oy   y-offset inside tile
     */
    public void placeAtTile(Vector2i tile, float ox, float oy) {
        checkTileNotNull(tile);
        placeAtTile(tile.x(), tile.y(), ox, oy);
    }

    /**
     * Places this creature centered over the given tile.
     *
     * @param tile tile where creature is placed
     */
    public void centerOverTile(Vector2i tile) {
        placeAtTile(tile, 0, 0);
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
            Logger.trace("{}: New moveDir: {}. {}", name, moveDir, this);
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
            Logger.trace("{}: New wishDir: {}. {}", name, wishDir, this);
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
     * Signals that this creature should reverse its move direction as soon as possible.
     */
    public void reverseAsSoonAsPossible() {
        gotReverseCommand = true;
        newTileEntered = false;
        Logger.trace("{} (moveDir={}, wishDir={}) got command to reverse direction", name, moveDir, wishDir);
    }

    /**
     * Sets the speed in percent of the base speed (1.25 pixels/second).
     *
     * @param percentage percentage of base speed
     */
    public void setPercentageSpeed(byte percentage) {
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
}