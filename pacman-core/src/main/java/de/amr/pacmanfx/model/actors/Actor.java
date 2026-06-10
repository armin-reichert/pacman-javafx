/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.AnimationFacade;
import de.amr.pacmanfx.model.world.WorldMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

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

    private float x;
    private float y;

    private float velX;
    private float velY;

    private float accX;
    private float accY;

    /**
     * Resets all properties of this actor thingy to their default state. Note: actor is invisible by default!
     */
    public void reset() {
        setVisible(DEFAULT_VISIBILITY);
        x = y = 0;
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

    public final void setVisible(boolean value) {
        if (visible == null && DEFAULT_VISIBILITY == value) return;
        visibleProperty().set(value);
    }

    public void show() {
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }

    public final void setX(double x) {
        this.x = (float) x;
    }

    public float x() {
        return x;
    }

    public final void setY(double y) {
        this.y = (float) y;
    }

    public float y() {
        return y;
    }

    public Vector2f position() {
        return new Vector2f(x, y);
    }

    public final void setPosition(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public final void setPosition(Vector2f position) {
        requireNonNull(position);
        setX(position.x());
        setY(position.y());
    }

    public float velX() {
        return velX;
    }

    public final void setVelX(double velX) {
        this.velX = (float) velX;
    }

    public float velY() {
        return velY;
    }

    public final void setVelY(double velY) {
        this.velY = (float) velY;
    }

    public final void setVelocity(double vx, double vy) {
        this.velX = (float) vx;
        this.velY = (float) vy;
    }

    public double computeSpeed() {
        return Math.hypot(velX, velY);
    }

    public float accX() {
        return accX;
    }

    public final void setAccX(float accX) {
        this.accX = accX;
    }

    public float accY() {
        return accY;
    }

    public final void setAccY(float accY) {
        this.accY = accY;
    }

    public final void setAcceleration(float ax, float ay) {
        this.accX = ax;
        this.accY = ay;
    }

    /**
     * An accelerated movement.
     * Changes the position of this actor by the current velocity vector and then increases the velocity
     * by the current acceleration.
     */
    public void move() {
        x += velX;
        y += velY;
        velX += accX;
        velY += accY;
    }

    /**
     * We define the position of each actor as the left-upper corner of a square with side-length 1 tile (8 pixels).
     * The center position of an actor is the center of this square. This has some advantages but also
     * some drawbacks, as everything in life.
     *
     * @return the center position of the actor
     */
    public Vector2f computeCenter() { return new Vector2f(x + WorldMap.HTS, y + WorldMap.HTS); }

    /**
     * In Pac-Man games, the current tile coordinate of an actor is defined as the tile containing the
     * actor's center position.
     *
     * @return the tile coordinate containing the {@link #computeCenter()} position of the actor.
     */
    public Vector2i computeTile() {
        final float cx = x + WorldMap.HTS;
        final float cy = y + WorldMap.HTS;
        return WorldMap.computeTileAt(cx, cy);
    }

    /**
     * @return x-offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public float computeOffsetX() {
        final Vector2i tile = computeTile();
        return x - tile.x() * WorldMap.TS;
    }

    /**
     * @return y-offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public float computeOffsetY() {
        final Vector2i tile = computeTile();
        return y - tile.y() * WorldMap.TS;
    }

    protected AnimationFacade animations = AnimationFacade.emptyAnimationFacade();

    public void setAnimations(AnimationFacade animationManager) {
        this.animations = animationManager;
    }

    public AnimationFacade animations() {
        return animations;
    }
}