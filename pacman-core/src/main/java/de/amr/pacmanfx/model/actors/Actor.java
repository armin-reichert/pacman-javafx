/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationID;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, velocity, acceleration and visibility.
 * </p>
 */
public class Actor {

    public static final boolean DEFAULT_VISIBILITY = false;

    private BooleanProperty visible;

    private FloatProperty x;
    private FloatProperty y;

    private float velX;
    private float velY;

    private float accX;
    private float accY;

    /**
     * Resets all properties of this actor thingy to their default state. Note: actor is invisible by default!
     */
    public void reset() {
        setVisible(DEFAULT_VISIBILITY);
        setX(0);
        setY(0);
        velX = velY = 0;
        accX = accY = 0;
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
            x = new SimpleFloatProperty(0);
        }
        return x;
    }

    public void setX(double value) {
        if (x == null && value == 0) return;
        xProperty().set((float) value);
    }

    public float x() {
        return x == null ? 0 : xProperty().get();
    }

    public FloatProperty yProperty() {
        if (y == null) {
            y = new SimpleFloatProperty(0);
        }
        return y;
    }

    public void setY(double value) {
        if (y == null && value == 0) return;
        yProperty().set((float) value);
    }

    public float y() {
        return y == null ? 0 : yProperty().get();
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

    /**
     * We define the position of each actor as the left-upper corner of a square with side-length 1 tile (8 pixels).
     * The center position of an actor is the center of this square. This has some advantages but also
     * some drawbacks, as everything in life.
     *
     * @return the center position of the actor
     */
    public Vector2f center() { return new Vector2f(x(), y()).plus(HTS, HTS); }

    public float velX() {
        return velX;
    }

    public void setVelX(double velX) {
        this.velX = (float) velX;
    }

    public float velY() {
        return velY;
    }

    public void setVelY(double velY) {
        this.velY = (float) velY;
    }

    public void setVelocity(double vx, double vy) {
        this.velX = (float) vx;
        this.velY = (float) vy;
    }

    public double computeSpeed() {
        return Math.hypot(velX, velY);
    }

    public float accX() {
        return accX;
    }

    public void setAccX(float accX) {
        this.accX = accX;
    }

    public float accY() {
        return accY;
    }

    public void setAccY(float accY) {
        this.accY = accY;
    }

    public void setAcceleration(float ax, float ay) {
        this.accX = ax;
        this.accY = ay;
    }

    /**
     * An accelerated movement.
     * Changes the position of this actor by the current velocity vector and then increases the velocity
     * by the current acceleration.
     */
    public void move() {
        setX(x() + velX);
        setY(y() + velY);
        velX += accX;
        velY += accY;
    }

    /**
     * In Pac-Man games, the current tile coordinate of an actor is defined as the tile containing the
     * actor's center position.
     *
     * @return the tile coordinate containing the {@link #center()} position of the actor.
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

    // --- Sprite animation support

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