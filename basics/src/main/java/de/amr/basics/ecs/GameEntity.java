/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ecs;

import de.amr.basics.Composition;
import de.amr.basics.Disposable;
import de.amr.basics.ecs.comp.LifetimeComp;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.ecs.comp.PositionComp;
import de.amr.basics.ecs.comp.VisibilityComp;
import de.amr.basics.timer.TickTimer;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Entities are composed of components. Entity "systems" are mostly stateless classes
 * working on entities.
 * <p>
 * Each entity by default contains the components "position" and "visibility".
 * </p>
 */
public class GameEntity extends Composition<Object> implements Disposable {

    protected String name;

    public GameEntity() {
        name = getClass().getSimpleName() + "#" + Integer.toHexString(hashCode()); // default name
        setComponent(PositionComp.class, new PositionComp());
        setComponent(VisibilityComp.class, new VisibilityComp(false));
    }

    // Typed access

    public final PositionComp pos() {
        return assertComponent(PositionComp.class);
    }

    public final VisibilityComp visibility() {
        return assertComponent(VisibilityComp.class);
    }

    public final Optional<MovementComp> optMovement() {
        return optComponent(MovementComp.class);
    }

    public final LifetimeComp lifetime() {
        return assertComponent(LifetimeComp.class);
    }

    public void setLifetimeSec(float seconds) {
        setComponent(LifetimeComp.class, new LifetimeComp(TickTimer.secToTicks(seconds)));
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
        componentsNoCopy().forEach(c -> {
            if (c instanceof Resettable resettable) {
                resettable.reset();
            }
        });
    }

    public final void show() {
        visibility().setVisible(true);
    }

    public final void hide() {
        visibility().setVisible(false);
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