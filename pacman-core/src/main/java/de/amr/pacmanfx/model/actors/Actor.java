/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
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
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, velocity, acceleration and visibility property.
 * </p>
 */
public class Actor {

    public static final Vector2f DEFAULT_ACCELERATION = Vector2f.ZERO;
    public static final Vector2f DEFAULT_POSITION = Vector2f.ZERO;
    public static final Vector2f DEFAULT_VELOCITY = Vector2f.ZERO;
    public static final boolean DEFAULT_VISIBILITY = false;

    private ObjectProperty<Vector2f> position;
    private BooleanProperty visible;
    private ObjectProperty<Vector2f> velocity;
    private ObjectProperty<Vector2f> acceleration;

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
        setPosition(position().plus(velocity()));
        setVelocity(velocity().plus(acceleration()));
    }

    /**
     * @return Tile containing the center of the actor's one-size-square "collision box". Actor position denotes
     *         the left-upper corner of that box
     */
    public Vector2i tile() {
        return tileAt(center());
    }

    /**
     * @return Offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public Vector2f offset() {
        return position().minus(tile().scaled((float)TS));
    }

    // Animation support

    protected AnimationManager animationManager = AnimationManager.EMPTY;

    public Optional<AnimationManager> optAnimationManager() {
        return Optional.ofNullable(animationManager);
    }

    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = animationManager != null ? animationManager : AnimationManager.EMPTY;
    }

    public void selectAnimation(Object animationID) {
        requireNonNull(animationID);
        animationManager.select(animationID);
    }

    public void selectAnimationAt(Object animationID, int frameIndex) {
        requireNonNull(animationID);
        animationManager.selectFrame(animationID, frameIndex);
    }

    public void playAnimation(Object animationID) {
        requireNonNull(animationID);
        animationManager.play(animationID);
    }

    public void playAnimation() {
        animationManager.play();
    }

    public void stopAnimation() {
        animationManager.stop();
    }
}