/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.basics.ecs;

import de.amr.basics.Composition;
import de.amr.basics.Disposable;
import de.amr.basics.ecs.comp.Lifetime;
import de.amr.basics.ecs.comp.Movement;
import de.amr.basics.ecs.comp.Position;
import de.amr.basics.ecs.comp.Visibility;
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
        setComponent(Position.class, new Position());
        setComponent(Visibility.class, new Visibility(false));
    }

    // Typed access

    public final Position position() {
        return assertComponent(Position.class);
    }

    public final Visibility visibility() {
        return assertComponent(Visibility.class);
    }

    public final Optional<Movement> optMovement() {
        return optComponent(Movement.class);
    }

    public final Lifetime lifetime() {
        return assertComponent(Lifetime.class);
    }

    public void setLifetimeInSeconds(float seconds) {
        setComponent(Lifetime.class, new Lifetime(TickTimer.secToTicks(seconds)));
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