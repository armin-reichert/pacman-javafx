/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.Disposable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;

import static java.util.Objects.requireNonNull;

/**
 * A component registry.
 *
 * @param <C> the component base type e.g. {@link de.amr.pacmanfx.core.ecs.EntityComponent}
 */
public class ComponentRegistry<C> implements Disposable {

    private final LinkedHashMap<Class<? extends C>, C> componentMap = new LinkedHashMap<>(10);

    public ComponentRegistry() {
    }

    protected SequencedCollection<C> componentsNoCopy() {
        return componentMap.sequencedValues();
    }

    /**
     * @return a copy of the set of registered components in insertion-order
     */
    public SequencedCollection<C> components() {
        return List.copyOf(componentMap.sequencedValues());
    }

    @Override
    public void dispose() {
        for (C component : componentsNoCopy()) {
            if (component instanceof Disposable) {
                ((Disposable) component).dispose();
            }
        }
        componentMap.clear();
    }

    /**
     * Sets the entity component of the given type. Throws an exception if there is already such a component registered.
     *
     * @param type the component type
     * @param component the component to be registered
     * @param <T> component type
     */
    public final <T extends C> void setComp(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (componentMap.containsKey(type)) {
            throw new IllegalArgumentException("Component for class: " + type.getSimpleName() + " is already registered!");
        }
        componentMap.put(type, component);
    }

    /**
     * Returns the entity component of the given type. Throws an exception if there is none.
     *
     * @param type the component type
     * @return the entity component registered for the given type
     * @param <T> component type
     */
    public final <T extends C> T reqComp(Class<T> type) {
        requireNonNull(type);
        if (!componentMap.containsKey(type)) {
            throw new IllegalArgumentException("No component found for class %s".formatted(type.getSimpleName()));
        }
        final C component = componentMap.get(type);
        return type.cast(component);
    }

    /**
     * Checks for the optional entity component of the given type. Returns {@code false} if no such component exists.
     *
     * @param type the component type
     * @return {@code true} if a component for this type is registered
     * @param <T> component type
     */
    public final <T extends C> boolean hasComp(Class<T> type) {
        requireNonNull(type);
        return componentMap.containsKey(type);
    }

    /**
     * Returns the optional entity component of the given type. Returns empty if no such component exists.
     *
     * @param type the component type
     * @return the entity component registered for the given type
     * @param <T> component type
     */
    public final <T extends C> Optional<T> optComp(Class<T> type) {
        requireNonNull(type);
        final C component = componentMap.get(type);
        return Optional.ofNullable(component).map(type::cast);
    }
}
