/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.component.*;
import de.amr.pacmanfx.core.model.level.GameEntity;
import de.amr.pacmanfx.core.model.level.GameLevel;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, movement and visibility component and access to sprite animations
 * in a UI independent way.
 * </p>
 */
public class Actor implements GameEntity {

    private final Map<Class<? extends EntityComponent>, EntityComponent> components = new HashMap<>();

    public SpriteAnimationAccess animations = SpriteAnimationAccess.emptyAnimation();

    protected String name;

    public Actor(String name) {
        this.name = requireNonNull(name);
        registerComponent(Position.class, new Position());
        registerComponent(Visibility.class, new Visibility(false));
    }

    public <T extends EntityComponent> void registerComponent(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            throw new IllegalArgumentException("Component for class %s is already registered".formatted(type.getSimpleName()));
        }
        components.put(type, component);
    }

    public <T extends EntityComponent> T component(Class<T> componentClass) {
        requireNonNull(componentClass);
        final EntityComponent component = components.get(componentClass);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(componentClass.getSimpleName()));
        }
        return componentClass.cast(component);
    }

    /**
     * @return readable name, used in UI and logging
     */
    public final String name() {
        return name;
    }

    public Position position() {
        return component(Position.class);
    }

    public Movement movement() {
        return component(Movement.class);
    }

    public Visibility visibility() {
        return component(Visibility.class);
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
        components.values().forEach(EntityComponent::reset);
    }
}