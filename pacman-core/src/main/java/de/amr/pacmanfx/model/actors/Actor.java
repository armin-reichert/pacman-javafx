/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tileAt;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors like Pac-Man. ghosts and bonus entities.
 * <p>
 * Each actor has a position, velocity, acceleration and visibility property.
 * </p>
 */
public class Actor {

    public static final Vector2f DEFAULT_ACCELERATION = Vector2f.ZERO;
    public static final Vector2f DEFAULT_POSITION = Vector2f.ZERO;
    public static final Vector2f DEFAULT_VELOCITY = Vector2f.ZERO;
    public static final boolean DEFAULT_VISIBILITY = false;

    protected final GameContext gameContext;

    private ObjectProperty<Vector2f> position;
    private BooleanProperty visible;
    private ObjectProperty<Vector2f> velocity;
    private ObjectProperty<Vector2f> acceleration;

    /**
     * @param gameContext the game context for this actor, may be null
     */
    public Actor(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public GameContext gameContext() {
        return gameContext;
    }

    public Optional<GameContext> optGameContext() {
        return Optional.ofNullable(gameContext);
    }

    /**
     * Resets all properties of this actor thingy to their default state. Note: actor is invisible by default!
     */
    public void reset() {
        setVisible(DEFAULT_VISIBILITY);
        setPosition(DEFAULT_POSITION);
        setVelocity(DEFAULT_VELOCITY);
        setAcceleration(DEFAULT_ACCELERATION);
    }

    public BooleanProperty visibleProperty() {
        if (visible == null) {
            visible = new SimpleBooleanProperty(DEFAULT_VISIBILITY);
        }
        return visible;
    }

    public boolean isVisible() {
        return visible == null ? DEFAULT_VISIBILITY : visibleProperty().get();
    }

    public void setVisible(boolean value) {
        if (visible == null && DEFAULT_VISIBILITY == value) return;
        visibleProperty().set(value);
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
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

    public ObjectProperty<Vector2f> positionProperty() {
        if (position == null) {
            position = new SimpleObjectProperty<>(DEFAULT_POSITION);
        }
        return position;
    }

    /**
     * @return upper left corner of the entity collision box which is a square of size 1 tile.
     */
    public Vector2f position() {
        return position == null ? DEFAULT_POSITION : positionProperty().get();
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2f(x, y));
    }

    public void setPosition(Vector2f pos) {
        requireNonNull(pos, "Position of actor must not be null");
        if (position == null && DEFAULT_POSITION.equals(pos)) return;
        positionProperty().set(pos);
    }

    /**
     * @return center of the entity collision box which is a square of size 1 tile.
     */
    public Vector2f center() { return position().plus(HTS, HTS); }

    public final ObjectProperty<Vector2f> velocityProperty() {
        if (velocity == null) {
            velocity = new SimpleObjectProperty<>(DEFAULT_VELOCITY);
        }
        return velocity;
    }

    public Vector2f velocity() {
        return velocity != null ? velocityProperty().get() : DEFAULT_VELOCITY;
    }

    public void setVelocity(Vector2f vector) {
        requireNonNull(vector, "Velocity vector must not be null");
        if (velocity == null && vector.equals(DEFAULT_VELOCITY)) return;
        velocityProperty().set(vector);
    }

    public void setVelocity(float vx, float vy) {
        setVelocity(new Vector2f(vx, vy));
    }

    public final ObjectProperty<Vector2f> accelerationProperty() {
        if (acceleration == null) {
            acceleration = new SimpleObjectProperty<>(DEFAULT_ACCELERATION);
        }
        return acceleration;
    }

    public Vector2f acceleration() {
        return acceleration != null ? accelerationProperty().get() : DEFAULT_ACCELERATION;
    }

    public void setAcceleration(Vector2f vector) {
        requireNonNull(vector, "Acceleration vector must not be null");
        if (acceleration == null && vector.equals(DEFAULT_ACCELERATION)) return;
        accelerationProperty().set(vector);
    }

    public void setAcceleration(float ax, float ay) {
        setAcceleration(new Vector2f(ax, ay));
    }

    /**
     * Moves this actor by its current velocity and increases its velocity by its current acceleration.
     */
    public void move() {
        if (velocity != null) {
            setPosition(position().plus(velocity()));
        }
        if (acceleration != null) {
            setVelocity(velocity().plus(acceleration()));
        }
    }

    /**
     * @return Tile containing the center of the actor's one-size-square "collision box". Actor position denotes
     *         the left-upper corner of that box
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
     * @return <code>true</code> if both entities have the same tile coordinate
     */
    public boolean atSameTileAs(Actor other) {
        requireNonNull(other, "Actor to check for same tile must not be null");
        return tile().equals(other.tile());
    }
}