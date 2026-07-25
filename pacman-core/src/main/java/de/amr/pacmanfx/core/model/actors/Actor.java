/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.component.Movement;
import de.amr.pacmanfx.core.model.component.Position;
import de.amr.pacmanfx.core.model.component.Visibility;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.core.model.level.GameEntity;
import de.amr.pacmanfx.core.model.level.GameLevel;

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
}