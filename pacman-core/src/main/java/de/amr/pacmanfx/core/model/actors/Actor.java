/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.component.Movement;
import de.amr.pacmanfx.core.model.component.Position;
import de.amr.pacmanfx.core.model.component.Visibility;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.core.model.level.GameEntity;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.WorldMap;

/**
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, movement and visibility component and access to sprite animations
 * in a UI independent way.
 * </p>
 */
public class Actor implements GameEntity {

    // These components will be stored in a map
    public final Position position = new Position();
    public final Movement movement = new Movement();
    public final Visibility visibility = new Visibility(false);
    public final WorldMovement worldMovement = new WorldMovement();

    public SpriteAnimationAccess animations = SpriteAnimationAccess.emptyAnimation();

    protected String name;

    public Actor(String name) {
        this.name = name;
    }

    /**
     * @return readable name, used in UI and logging
     */
    public final String name() {
        return name;
    }

    /**
     * @param level the game level we are in (not null)
     * @param tile some tile inside or outside the world
     * @return if this actor can access the given tile in its game context
     */
    public boolean canAccessTile(GameLevel level, Vector2i tile) {
        return true;
    }

    /**
     * @return {@code true} if this actor can reverse ist direction in its current state
     */
    public boolean canTurnBack() {
        return false;
    }

    /**
     * Resets this actor's components (position, movement, visibility) to their default values.
     * Note: actor is invisible by default!
     */
    public void reset() {
        position.reset();
        movement.reset();
        visibility.reset();
        WorldMovement.SYSTEM.reset(this);
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
}