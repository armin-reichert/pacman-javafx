/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs;

import de.amr.pacmanfx.core.ecs.components.MovementComp;
import de.amr.pacmanfx.core.ecs.components.PositionComp;
import de.amr.pacmanfx.core.ecs.components.VisibilityComp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Game entities are composed of entity (pure data) components and there are entity "systems" working on them.
 */
public class GameEntity {

    private final Map<Class<? extends GameEntityComponent>, GameEntityComponent> components = new LinkedHashMap<>(7);

    protected String name;

    public GameEntity() {
        name = getClass().getSimpleName() + "#" + Integer.toHexString(hashCode()); // default name
        setComponent(PositionComp.class, new PositionComp());
        setComponent(VisibilityComp.class, new VisibilityComp(false));
    }

    public final <T extends GameEntityComponent> void setComponent(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            throw new IllegalArgumentException("Component for class: " + type.getSimpleName() + " is already registered!");
        }
        components.put(type, component);
    }

    public final <T extends GameEntityComponent> T requireComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final GameEntityComponent component = components.get(componentClass);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(componentClass.getSimpleName()));
        }
        return componentClass.cast(component);
    }

    public final <T extends GameEntityComponent> boolean hasComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        return components.get(componentClass) != null;
    }

    public final <T extends GameEntityComponent> Optional<T> optComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final GameEntityComponent component = components.get(componentClass);
        return Optional.ofNullable(component).map(componentClass::cast);
    }

    // Component API

    public final PositionComp pos() {
        return requireComponent(PositionComp.class);
    }

    public final VisibilityComp visibility() {
        return requireComponent(VisibilityComp.class);
    }

    public final Optional<MovementComp> optMovement() {
        return optComponent(MovementComp.class);
    }

    public final void setName(String name) {
        this.name = requireNonNull(name);
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
        components.values().forEach(GameEntityComponent::reset);
    }

    public final void show() {
        visibility().set(true);
    }

    public final void hide() {
        visibility().set(false);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("{name=").append(name).append(", components=[");
        boolean first = true;
        for (var component : components.values()) {
            if (!first) b.append(", ");
            b.append(component);
            first = false;
        }
        b.append("]}");
        return b.toString();
    }
}