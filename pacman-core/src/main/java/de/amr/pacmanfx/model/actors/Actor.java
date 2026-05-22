/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationID;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import javafx.beans.property.*;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, velocity, acceleration and visibility property.
 * </p>
 */
public class Actor {

    public static final Vector2f DEFAULT_ACCELERATION = Vector2f.ZERO;
    public static final Vector2f DEFAULT_VELOCITY = Vector2f.ZERO;
    public static final boolean DEFAULT_VISIBILITY = false;
    public static final float DEFAULT_X = 0;
    public static final float DEFAULT_Y = 0;

    private BooleanProperty visible;

    private FloatProperty x;
    private FloatProperty y;

    private ObjectProperty<Vector2f> velocity;
    private ObjectProperty<Vector2f> acceleration;

    /**
     * Resets all properties of this actor thingy to their default state. Note: actor is invisible by default!
     */
    public void reset() {
        setVisible(DEFAULT_VISIBILITY);
        setX(DEFAULT_X);
        setY(DEFAULT_Y);
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

    public FloatProperty xProperty() {
        if (x == null) {
            x = new SimpleFloatProperty(DEFAULT_X);
        }
        return x;
    }

    public void setX(double value) {
        if (x == null && value == DEFAULT_X) return;
        xProperty().set((float) value);
    }

    public float x() {
        return x == null ? DEFAULT_X : xProperty().get();
    }

    public FloatProperty yProperty() {
        if (y == null) {
            y = new SimpleFloatProperty(DEFAULT_Y);
        }
        return y;
    }

    public void setY(double value) {
        if (y == null && value == DEFAULT_Y) return;
        yProperty().set((float) value);
    }

    public float y() {
        return y == null ? DEFAULT_Y : yProperty().get();
    }

    public Vector2f position() {
        return new Vector2f(x(), y());
    }

    public void setPosition(double x, double y) {
        setX(x);
        setY(y);
    }

    public void setPosition(Vector2f position) {
        requireNonNull(position);
        setX(position.x());
        setY(position.y());
    }

    public Vector2f center() { return new Vector2f(x(), y()).plus(HTS, HTS); }

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
        setX(x() + velocity().x());
        setY(y() + velocity().y());
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
        final Vector2i tile = tile();
        final float ox = x() - tile.x() * TS;
        final float oy = y() - tile.y() * TS;
        return new Vector2f(ox, oy);
    }

    // Animation support

    protected SpriteAnimationSet animations = SpriteAnimationSet.emptyAnimSet();

    public SpriteAnimationSet animations() {
        return animations;
    }

    public void setAnimations(SpriteAnimationSet animations) {
        this.animations = requireNonNull(animations);
    }

    public void selectAnimation(SpriteAnimationID animationID) {
        requireNonNull(animationID);
        animations.selectAnimation(animationID);
    }

    public void selectAnimationAtFrame(SpriteAnimationID animationID, int frameIndex) {
        requireNonNull(animationID);
        animations.setAnimationFrame(animationID, frameIndex);
    }

    public void playAnimation() {
        animations.playSelectedAnimation();
    }

    public void stopAnimation() {
        animations.stopSelectedAnimation();
    }

    public void resetAnimation() {
        animations.resetSelectedAnimation();
    }
}