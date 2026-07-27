/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.basics.spriteanim.SpriteAnimationAccess;
import de.amr.pacmanfx.core.model.component.EntityComponent;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.common.Position;
import de.amr.pacmanfx.core.model.component.common.Visibility;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Base class for all game actors like Pac-Man, the ghosts and the bonus entities.
 * <p>
 * Each actor has a position, movement and visibility component and access to sprite animations
 * in a UI independent way.
 * </p>
 */
public class Actor {

    private final Map<Class<? extends EntityComponent>, EntityComponent> components = new LinkedHashMap<>();

    public SpriteAnimationAccess animations = SpriteAnimationAccess.emptyAnimation();

    protected String name;

    public Actor() {
        registerComponent(Position.class, new Position());
        registerComponent(Visibility.class, new Visibility(false));

        name = getClass().getSimpleName() + "#" + hashCode();
    }

    public <T extends EntityComponent> void registerComponent(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            throw new IllegalArgumentException("Component for class %s is already registered".formatted(type.getSimpleName()));
        }
        components.put(type, component);
    }

    public <T extends EntityComponent> T assertComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final EntityComponent component = components.get(componentClass);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(componentClass.getSimpleName()));
        }
        return componentClass.cast(component);
    }

    public <T extends EntityComponent> boolean hasComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        return components.get(componentClass) != null;
    }


    public final Position position() {
        return assertComponent(Position.class);
    }

    public final Movement movement() {
        return assertComponent(Movement.class);
    }

    public final Visibility visibility() {
        return assertComponent(Visibility.class);
    }

    /**
     * @return readable name, used in UI and logging
     */
    public final String name() {
        return name;
    }

    /**
     * Resets this actor's components (position, movement, visibility) to their default values.
     * Note: actor is invisible by default!
     */
    public void reset() {
        components.values().forEach(EntityComponent::reset);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (var component : components.values()) {
            builder.append("{").append(component).append("}\n");
        }
        return builder.toString();
    }
}