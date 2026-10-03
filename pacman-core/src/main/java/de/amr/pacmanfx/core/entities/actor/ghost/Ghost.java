/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.actor.ghost;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.ui.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.entities.world.WorldNavigationComp;
import de.amr.pacmanfx.core.model.GhostPersonality;

import static java.util.Objects.requireNonNull;

/**
 * A ghost. Ghosts differ in their personality which defines attack behavior and look.
 */
public final class Ghost extends GameEntity {

    private final GhostPersonality personality;

    public Ghost(GhostPersonality personality, String name) {
        this.personality = requireNonNull(personality);
        setName(name);

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(GhostWorldInfoComp.class, new GhostWorldInfoComp());
        setComponent(GhostHouseAccessComp.class, new GhostHouseAccessComp());
        setComponent(GhostStateComp.class, new GhostStateComp());
        setComponent(GhostAnimationComp.class, new GhostAnimationComp());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());

        //TODO where does this belong?
        worldNavigation().corneringSpeedDelta = -1.25f;
    }

    public GhostPersonality personality() {
        return personality;
    }

    // Typed component accessors

    public MovementComp movement() {
        return assertComponent(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return assertComponent(WorldNavigationComp.class);
    }

    public GhostWorldInfoComp worldInfo() {
        return assertComponent(GhostWorldInfoComp.class);
    }

    public GhostHouseAccessComp houseAccess() {
        return assertComponent(GhostHouseAccessComp.class);
    }

    public GhostStateComp state() {
        return assertComponent(GhostStateComp.class);
    }

    public GhostAnimationComp animation() {
        return assertComponent(GhostAnimationComp.class);
    }

    public SpriteAnimationComp spriteAnimation() {
        return assertComponent(SpriteAnimationComp.class);
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "personality=" + personality +
            ", state=" + state().enumValue() +
            ", " + super.toString() +
            '}';
    }
}