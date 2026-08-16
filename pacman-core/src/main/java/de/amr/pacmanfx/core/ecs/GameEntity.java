/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.ComponentRegistry;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;
import de.amr.pacmanfx.core.ecs.comp.VisibilityComp;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Game entities are composed of entity (pure data) components. Entity "systems" use these components.
 * <p>
 * Each game entity by default contains the components "position" and "visibility".
 * </p>
 */
public class GameEntity implements Disposable {

    private final ComponentRegistry componentRegistry = new ComponentRegistry();

    protected String name;

    public GameEntity() {
        name = getClass().getSimpleName() + "#" + Integer.toHexString(hashCode()); // default name
        setComp(PositionComp.class, new PositionComp());
        setComp(VisibilityComp.class, new VisibilityComp(false));
    }

    public final <T extends GameEntityComponent> void setComp(Class<T> type, T component) {
        componentRegistry.setComp(type, component);
    }

    public final <T extends GameEntityComponent> T requireComp(Class<T> type) {
        return componentRegistry.requireComp(type);
    }

    public final <T extends GameEntityComponent> boolean hasComp(Class<T> type) {
        return componentRegistry.hasComp(type);
    }

    public final <T extends GameEntityComponent> Optional<T> optComp(Class<T> type) {
        return componentRegistry.optComp(type);
    }

    // Typed access

    public final PositionComp pos() {
        return requireComp(PositionComp.class);
    }

    public final VisibilityComp visibility() {
        return requireComp(VisibilityComp.class);
    }

    public final Optional<MovementComp> optMovement() {
        return optComp(MovementComp.class);
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
     * Resets all components (position, visibility etc.) to their default values.
     */
    public void reset() {
        componentRegistry.components().forEach(GameEntityComponent::reset);
    }

    public final void show() {
        visibility().set(true);
    }

    public final void hide() {
        visibility().set(false);
    }

    public final boolean isVisible() {
        return visibility().isVisible();
    }

    @Override
    public void dispose() {
        for (GameEntityComponent comp : componentRegistry.components()) {
            if (comp instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("{name=").append(name);
        b.append(", components=[");
        boolean first = true;
        for (var component : componentRegistry.components()) {
            if (!first) b.append(", ");
            b.append(component);
            first = false;
        }
        b.append("]}");
        return b.toString();
    }
}