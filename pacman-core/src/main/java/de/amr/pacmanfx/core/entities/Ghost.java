/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostAnimationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostWorldPlacementComp;
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
        setComponent(GhostWorldPlacementComp.class, new GhostWorldPlacementComp());
        setComponent(GhostStateComp.class, new GhostStateComp());
        setComponent(GhostAnimationComp.class, new GhostAnimationComp());
        setComponent(SpriteAnimationComp.class, new SpriteAnimationComp());

        //TODO where does this belong?
        worldNavigation().corneringSpeedDelta = -1.25f;
    }

    public GhostPersonality personality() {
        return personality;
    }

    public GhostState ghostStateEnum() {
        return state().ghostStateEnum();
    }

    // Typed component accessors

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return requireComponent(WorldNavigationComp.class);
    }

    public GhostWorldPlacementComp worldPlacement() {
        return requireComponent(GhostWorldPlacementComp.class);
    }

    public GhostStateComp state() {
        return requireComponent(GhostStateComp.class);
    }

    public GhostAnimationComp ghostAnimation() {
        return requireComponent(GhostAnimationComp.class);
    }

    public SpriteAnimationComp spriteAnimation() {
        return requireComponent(SpriteAnimationComp.class);
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "personality=" + personality +
            ", state=" + ghostStateEnum() +
            ", " + super.toString() +
            '}';
    }
}