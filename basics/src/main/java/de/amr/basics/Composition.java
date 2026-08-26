/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.SequencedCollection;

import static java.util.Objects.requireNonNull;

/**
 * A component composition
 *
 * @param <C> the common component base type
 */
public class Composition<C> implements Disposable {

    private final LinkedHashMap<Class<? extends C>, C> componentsByType = new LinkedHashMap<>(10);

    public Composition() {}

    protected SequencedCollection<C> componentsNoCopy() {
        return componentsByType.sequencedValues();
    }

    /**
     * @return a copy of the set of registered components in insertion-order
     */
    public SequencedCollection<C> components() {
        return List.copyOf(componentsByType.sequencedValues());
    }

    @Override
    public void dispose() {
        for (C component : componentsNoCopy()) {
            if (component instanceof Disposable) {
                ((Disposable) component).dispose();
            }
        }
        componentsByType.clear();
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
        if (componentsByType.containsKey(type)) {
            throw new IllegalArgumentException("Component for class: " + type.getSimpleName() + " is already registered!");
        }
        componentsByType.put(type, component);
    }

    public final <T extends C> void removeComp(Class<T> type) {
        if (hasComp(type)) {
            var comp = componentsByType.remove(type);
            if (comp instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
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
        if (!componentsByType.containsKey(type)) {
            throw new IllegalArgumentException("No component found for class %s".formatted(type.getSimpleName()));
        }
        final C component = componentsByType.get(type);
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
        return componentsByType.containsKey(type);
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
        final C component = componentsByType.get(type);
        return Optional.ofNullable(component).map(type::cast);
    }
}
