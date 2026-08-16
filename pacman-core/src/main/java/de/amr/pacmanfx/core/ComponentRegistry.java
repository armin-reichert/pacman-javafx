/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core;

import de.amr.basics.Disposable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ComponentRegistry<C> implements Disposable {

    private final Map<Class<? extends C>, C> components = new LinkedHashMap<>(7);

    public ComponentRegistry() {
    }

    public Collection<C> components() {
        return components.values();
    }

    @Override
    public void dispose() {
        for (C component : components.values()) {
            if (component instanceof Disposable) {
                ((Disposable) component).dispose();
            }
        }
        components.clear();
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
