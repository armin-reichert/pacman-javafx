/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationAccessor;
import de.amr.pacmanfx.core.model.Movement;
import de.amr.pacmanfx.core.model.Position;
import de.amr.pacmanfx.core.model.world.WorldMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, velocity, acceleration and visibility.
 * </p>
 */
public class Actor {

    public static final boolean DEFAULT_VISIBILITY = false;

    public final Position position = new Position();
    public final Movement movement = new Movement();

    private BooleanProperty visible;

    /**
     * Resets all properties of this actor thingy to their default state. Note: actor is invisible by default!
     */
    public void reset() {
        setVisible(DEFAULT_VISIBILITY);

        position.x = 0;
        position.y = 0;

        movement.velX = 0;
        movement.velY = 0;
        movement.accX = 0;
        movement.accY = 0;
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

    /**
     * An accelerated movement.
     * Changes the position of this actor by the current velocity vector and then increases the velocity
     * by the current acceleration.
     */
    public void move() {
        movement.move(position);
    }

    /**
     * We define the position of each actor as the left-upper corner of a square with side-length 1 tile (8 pixels).
     * The center position of an actor is the center of this square. This has some advantages but also
     * some drawbacks, as everything in life.
     *
     * @return the center position of the actor
     */
    public Vector2f computeCenter() {
        return new Vector2f(position.x + WorldMap.HTS, position.y + WorldMap.HTS); }

    /**
     * In Pac-Man games, the current tile coordinate of an actor is defined as the tile containing the
     * actor's center position.
     *
     * @return the tile coordinate containing the {@link #computeCenter()} position of the actor.
     */
    public Vector2i computeTile() {
        final float cx = position.x + WorldMap.HTS;
        final float cy = position.y + WorldMap.HTS;
        return WorldMap.computeTileAt(cx, cy);
    }

    /**
     * @return x-offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public float computeOffsetX() {
        final Vector2i tile = computeTile();
        return position.x - tile.x() * WorldMap.TS;
    }

    /**
     * @return y-offset inside current tile: (0, 0) if centered, range: [-4, +4)
     */
    public float computeOffsetY() {
        final Vector2i tile = computeTile();
        return position.y - tile.y() * WorldMap.TS;
    }

    protected SpriteAnimationAccessor animations = SpriteAnimationAccessor.emptyAnimation();

    public void setAnimations(SpriteAnimationAccessor animations) {
        this.animations = animations;
    }

    public SpriteAnimationAccessor animations() {
        return animations;
    }
}