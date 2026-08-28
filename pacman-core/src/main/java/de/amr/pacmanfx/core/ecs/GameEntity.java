/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.ecs;

import de.amr.basics.Composition;
import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.PositionComp;
import de.amr.pacmanfx.core.ecs.comp.VisibilityComp;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Entities are composed of components. Entity "systems" are mostly stateless classes
 * working on entities.
 * <p>
 * Each entity by default contains the components "position" and "visibility".
 * </p>
 */
public class GameEntity extends Composition<GameEntityComp> implements Disposable {

    protected String name;

    public GameEntity() {
        name = getClass().getSimpleName() + "#" + Integer.toHexString(hashCode()); // default name
        setComp(PositionComp.class, new PositionComp());
        setComp(VisibilityComp.class, new VisibilityComp(false));
    }

    // Typed access

    public final PositionComp pos() {
        return reqComp(PositionComp.class);
    }

    public final VisibilityComp visibility() {
        return reqComp(VisibilityComp.class);
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
        componentsNoCopy().forEach(GameEntityComp::reset);
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
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append("{name=").append(name);
        b.append(", components=[");
        boolean first = true;
        for (var component : componentsNoCopy()) {
            if (!first) b.append(", ");
            b.append(component);
            first = false;
        }
        b.append("]}");
        return b.toString();
    }
}