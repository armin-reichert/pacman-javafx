package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.GameEntityComponent;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ComponentRegistry {

    private final Map<Class<? extends GameEntityComponent>, GameEntityComponent> components = new LinkedHashMap<>(7);

    public Collection<GameEntityComponent> components() {
        return components.values();
    }

    public final <T extends GameEntityComponent> void setComp(Class<T> type, T component) {
        requireNonNull(type);
        requireNonNull(component);
        if (components.containsKey(type)) {
            throw new IllegalArgumentException("Component for class: " + type.getSimpleName() + " is already registered!");
        }
        components.put(type, component);
    }

    public final <T extends GameEntityComponent> T requireComp(Class<T> type) {
        requireNonNull(type);
        final GameEntityComponent component = components.get(type);
        if (component == null) {
            throw new IllegalArgumentException("No component found for class %s".formatted(type.getSimpleName()));
        }
        return type.cast(component);
    }

    public final <T extends GameEntityComponent> boolean hasComp(Class<T> type) {
        requireNonNull(type);
        return components.get(type) != null;
    }

    public final <T extends GameEntityComponent> Optional<T> optComp(Class<T> type) {
        requireNonNull(type);
        final GameEntityComponent component = components.get(type);
        return Optional.ofNullable(component).map(type::cast);
    }
}
