/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

import static de.amr.games.pacman.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors, e.g. creatures and bonus entities.
 * <p>
 * Each actor has a position velocity, acceleration (all sub-pixel precision) and a visibility.
 * </p>
 *
 * @author Armin Reichert
 */
public class Actor {

    protected boolean visible;
    protected float posX, posY;
    protected float velX, velY;
    protected float accX, accY;

    @Override
    public String toString() {
        return "Actor{" +
            "visible=" + visible +
            ", posX=" + posX +
            ", posY=" + posY +
            ", velX=" + velX +
            ", velY=" + velY +
            ", accX=" + accX +
            ", accY=" + accY +
            '}';
    }

    /**
     * Resets this actor thingy to its initial state, not that it is invisible by default!
     */
    public void reset() {
        visible = false;
        posX = posY = velX = velY = accX = accY = 0;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void setPosX(float value) {
        posX = value;
    }

    public float posX() {
        return posX;
    }

    public void setPosY(float value) {
        posY = value;
    }

    public float posY() {
        return posY;
    }

    /**
     * @return upper left corner of the entity collision box which is a square of size one tile.
     */
    public Vector2f position() {
        return Vector2f.of(posX, posY);
    }

    public void setPosition(float x, float y) {
        posX = x;
        posY = y;
    }

    public void setPosition(Vector2f position) {
        requireNonNull(position, "Position of actor must not be null");
        posX = position.x();
        posY = position.y();
    }

    public Vector2f velocity() {
        return Vector2f.of(velX, velY);
    }

    public void setVelocity(Vector2f velocity) {
        requireNonNull(velocity, "Velocity of actor must not be null");
        velX = velocity.x();
        velY = velocity.y();
    }

    public void setVelocity(float vx, float vy) {
        velX = vx;
        velY = vy;
    }

    public Vector2f acceleration() {
        return Vector2f.of(accX, accY);
    }

    public void setAcceleration(Vector2f acceleration) {
        requireNonNull(acceleration, "Acceleration of actor must not be null");
        accX = acceleration.x();
        accY = acceleration.y();
    }

    public void setAcceleration(float ax, float ay) {
        accX = ax;
        accY = ay;
    }

    /**
     * Moves this actor by its current velocity and increases its velocity by its current acceleration.
     */
    public void move() {
        posX += velX;
        posY += velY;
        velX += accX;
        velY += accY;
    }

    /**
     * @return Tile containing the center of the actor collision box.
     */
    public Vector2i tile() {
        return tileAt(posX + HTS, posY + HTS);
    }

    /**
     * @return Offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public Vector2f offset() {
        var tile = tile();
        return Vector2f.of(posX - TS * tile.x(), posY - TS * tile.y());
    }

    /**
     * @param other some actor
     * @return <code>true</code> if both entities occupy same tile
     */
    public boolean sameTile(Actor other) {
        requireNonNull(other, "Actor to check for same tile must not be null");
        return tile().equals(other.tile());
    }
}