/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.model.component.ActorComponent;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.common.Position;
import de.amr.pacmanfx.core.model.component.common.Visibility;
import org.tinylog.Logger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class Actor {

    private final Map<Class<? extends ActorComponent>, ActorComponent> components = new LinkedHashMap<>();

    protected String name;

    public Actor() {
        name = super.toString(); // default name

        setComponent(Position.class, new Position());
        setComponent(Visibility.class, new Visibility(false));
    }

    public <T extends ActorComponent> void setComponent(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            Logger.warn("Component for class {} is already registered! Not overwritten!", type.getSimpleName());
            return;
        }
        components.put(type, component);
    }

    public <T extends ActorComponent> T assertComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final ActorComponent component = components.get(componentClass);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(componentClass.getSimpleName()));
        }
        return componentClass.cast(component);
    }

    public <T extends ActorComponent> boolean hasComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        return components.get(componentClass) != null;
    }

    public <T extends ActorComponent> Optional<T> optComponent(Class<T> componentClass) {
        requireNonNull(componentClass);
        final ActorComponent component = components.get(componentClass);
        return Optional.ofNullable(componentClass.cast(component));
    }

    public final Position position() {
        return assertComponent(Position.class);
    }

    public final Visibility visibility() {
        return assertComponent(Visibility.class);
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
        components.values().forEach(ActorComponent::reset);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("name=").append(name);
        for (var component : components.values()) {
            builder.append("[").append(component).append("]\n");
        }
        return builder.toString();
    }
}