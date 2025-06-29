/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

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
    private final ObjectProperty<Vector2f> positionProperty = new SimpleObjectProperty<>(Vector2f.ZERO);
    private final ObjectProperty<Vector2f> velocityProperty = new SimpleObjectProperty<>(Vector2f.ZERO);
    private final ObjectProperty<Vector2f> accelerationProperty = new SimpleObjectProperty<>(Vector2f.ZERO);

    @Override
    public String toString() {
        return "Actor{" +
               "visible=" + visible +
                ", position=" + position() +
                ", velocity=" + velocity() +
                ", acceleration=" + acceleration() +
                '}';
    }

    /**
     * Resets this actor thingy to its initial state, not that it is invisible by default!
     */
    public void reset() {
        visible = false;
        setPosition(Vector2f.ZERO);
        setVelocity(Vector2f.ZERO);
        setAcceleration(Vector2f.ZERO);
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

    public void setX(double x) {
        setPosition((float) x, position().y());
    }

    public float x() {
        return position().x();
    }

    public void setY(double y) {
        setPosition(position().x(), (float) y);
    }

    public float y() {
        return position().y();
    }

    /**
     * @return upper left corner of the entity collision box which is a square of size 1 tile.
     */
    public Vector2f position() { return positionProperty.get(); }

    /**
     * @return center of the entity collision box which is a square of size 1 tile.
     */
    public Vector2f center() { return position().plus(HTS, HTS); }

    public void setPosition(float x, float y) {
        setPosition(new Vector2f(x, y));
    }

    public void setPosition(Vector2f position) {
        requireNonNull(position, "Position of actor must not be null");
        positionProperty.set(position);
    }

    public Vector2f velocity() {
        return velocityProperty.get();
    }

    public void setVelocity(Vector2f velocity) {
        requireNonNull(velocity, "Velocity of actor must not be null");
        velocityProperty.set(velocity);
    }

    public void setVelocity(float vx, float vy) {
        setVelocity(new Vector2f(vx, vy));
    }

    public Vector2f acceleration() {
        return accelerationProperty.get();
    }

    public void setAcceleration(Vector2f acceleration) {
        requireNonNull(acceleration, "Acceleration vector must not be null");
        accelerationProperty.set(acceleration);
    }

    public void setAcceleration(float ax, float ay) {
        setAcceleration(new Vector2f(ax, ay));
    }

    public ObjectProperty<Vector2f> accelerationProperty() {
        return accelerationProperty;
    }

    /**
     * Moves this actor by its current velocity and increases its velocity by its current acceleration.
     */
    public void move() {
        setPosition(position().plus(velocity()));
        setVelocity(velocity().plus(acceleration()));
    }

    /**
     * @return Tile containing the center of the actor collision box.
     */
    public Vector2i tile() {
        Vector2f position = position();
        return tileAt(position.x() + HTS, position.y() + HTS);
    }

    /**
     * @return Offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public Vector2f offset() {
        return position().minus(tile().scaled((float)TS));
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