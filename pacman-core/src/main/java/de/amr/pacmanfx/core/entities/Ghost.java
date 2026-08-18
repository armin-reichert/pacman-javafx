/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostSpriteAnimationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostWorldInfoComp;
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

        setComp(MovementComp.class, new MovementComp());
        setComp(WorldNavigationComp.class, new WorldNavigationComp());
        setComp(GhostWorldInfoComp.class, new GhostWorldInfoComp());
        setComp(GhostStateComp.class, new GhostStateComp());
        setComp(GhostSpriteAnimationComp.class, new GhostSpriteAnimationComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());

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
        return reqComp(MovementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return reqComp(WorldNavigationComp.class);
    }

    public GhostWorldInfoComp worldInfo() {
        return reqComp(GhostWorldInfoComp.class);
    }

    public GhostStateComp state() {
        return reqComp(GhostStateComp.class);
    }

    public GhostSpriteAnimationComp ghostAnimation() {
        return reqComp(GhostSpriteAnimationComp.class);
    }

    public SpriteAnimationComp spriteAnimation() {
        return reqComp(SpriteAnimationComp.class);
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