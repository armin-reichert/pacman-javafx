/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors like Pac-Man. ghosts and bonus entities.
 * <p>
 * Each actor has a position, velocity, acceleration and visibility.
 * </p>
 */
public class Actor {

    protected boolean visible;
    protected float x, y;
    protected float vx, vy;
    protected float ax, ay;

    @Override
    public String toString() {
        return "Actor{" +
            "visible=" + visible +
            ", x=" + x +
            ", y=" + y +
            ", vx=" + vx +
            ", vy=" + vy +
            ", ax=" + ax +
            ", ay=" + ay +
            '}';
    }

    /**
     * Resets this actor thingy to its initial state, not that it is invisible by default!
     */
    public void reset() {
        visible = false;
        x = y = vx = vy = ax = ay = 0;
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

    public void setX(float value) {
        x = value;
    }

    public float x() {
        return x;
    }

    public void setY(float value) {
        y = value;
    }

    public float y() {
        return y;
    }

    /**
     * @return upper left corner of the entity collision box which is a square of size one tile.
     */
    public Vector2f position() {
        return Vector2f.of(x, y);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setPosition(Vector2f position) {
        requireNonNull(position, "Position of actor must not be null");
        x = position.x();
        y = position.y();
    }

    public Vector2f velocity() {
        return Vector2f.of(vx, vy);
    }

    public void setVelocity(Vector2f velocity) {
        requireNonNull(velocity, "Velocity of actor must not be null");
        vx = velocity.x();
        vy = velocity.y();
    }

    public void setVelocity(float vx, float vy) {
        this.vx = vx;
        this.vy = vy;
    }

    public Vector2f acceleration() {
        return Vector2f.of(ax, ay);
    }

    public void setAcceleration(Vector2f acceleration) {
        requireNonNull(acceleration, "Acceleration of actor must not be null");
        ax = acceleration.x();
        ay = acceleration.y();
    }

    public void setAcceleration(float ax, float ay) {
        this.ax = ax;
        this.ay = ay;
    }

    /**
     * Moves this actor by its current velocity and increases its velocity by its current acceleration.
     */
    public void move() {
        x += vx;
        y += vy;
        vx += ax;
        vy += ay;
    }

    /**
     * @return Tile containing the center of the actor collision box.
     */
    public Vector2i tile() {
        return tileAt(x + HTS, y + HTS);
    }

    /**
     * @return Offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public Vector2f offset() {
        var tile = tile();
        return Vector2f.of(x - TS * tile.x(), y - TS * tile.y());
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