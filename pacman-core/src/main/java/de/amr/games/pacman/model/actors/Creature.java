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
public abstract class Creature extends Move{



    /**
     *  @param name Readable name, for display and logging purposes.
     */
    protected Creature(String name) {
        checkNotNull(name, "Name of creature must not be null");
        this.name = name;
    }

    public String name() {
        return name;
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


    public void setWorld(World world) {
        checkNotNull(world);
        this.world = world;
    }

    public abstract boolean canReverse();



    /**
     * Tells if the creature entered a new tile with its last move or placement.
     */
    public boolean isNewTileEntered() {
        return newTileEntered;
    }

    public void setCanTeleport(boolean canTeleport) {
        this.canTeleport = canTeleport;
    }

    public boolean canTeleport() {
        return canTeleport;
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