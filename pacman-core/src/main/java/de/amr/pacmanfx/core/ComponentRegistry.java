/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ComponentRegistry<C> {

    private final Map<Class<? extends C>, C> components = new LinkedHashMap<>(7);

    public Collection<C> components() {
        return components.values();
    }

    public final <T extends C> void setComp(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            throw new IllegalArgumentException("Component for class: " + type.getSimpleName() + " is already registered!");
        }
        components.put(type, component);
    }

    public final <T extends C> T requireComp(Class<T> type) {
        requireNonNull(type);
        final C component = components.get(type);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(type.getSimpleName()));
        }
        return type.cast(component);
    }

    public final <T extends C> boolean hasComp(Class<T> type) {
        requireNonNull(type);
        return components.get(type) != null;
    }

    public final <T extends C> Optional<T> optComp(Class<T> type) {
        requireNonNull(type);
        final C component = components.get(type);
        return Optional.ofNullable(component).map(type::cast);
    }
}
