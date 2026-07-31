/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model;

import de.amr.pacmanfx.core.model.comp.common.PositionComp;
import de.amr.pacmanfx.core.model.comp.common.VisibilityComp;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Game entities are composed of entity (pure data) components and there are entity "systems" working on them.
 */
public class GameEntity {

    private final Map<Class<? extends GameEntityComponent>, GameEntityComponent> components = new LinkedHashMap<>();

    protected String name;

    public GameEntity() {
        name = getClass().getSimpleName() + "#" + Integer.toHexString(hashCode()); // default name

        setComponent(PositionComp.class, new PositionComp());
        setComponent(VisibilityComp.class, new VisibilityComp(false));
    }

    public <T extends GameEntityComponent> void setComponent(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            throw new IllegalArgumentException("Component for class: " + type.getSimpleName() + " is already registered!");
        }
        components.put(type, component);
    }

    public <T extends GameEntityComponent> T requireComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final GameEntityComponent component = components.get(componentClass);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(componentClass.getSimpleName()));
        }
        return componentClass.cast(component);
    }

    public <T extends GameEntityComponent> boolean hasComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        return components.get(componentClass) != null;
    }

    public <T extends GameEntityComponent> Optional<T> optComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final GameEntityComponent component = components.get(componentClass);
        return Optional.ofNullable(component).map(componentClass::cast);
    }

    public final PositionComp pos() {
        return requireComponent(PositionComp.class);
    }

    public final VisibilityComp visibility() {
        return requireComponent(VisibilityComp.class);
    }

    public void setName(String name) {
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

    public void show() {
        visibility().set(true);
    }

    public void hide() {
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