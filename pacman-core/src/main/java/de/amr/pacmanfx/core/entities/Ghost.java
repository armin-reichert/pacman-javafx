/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.*;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostAnimationComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostHouseAccessComp;
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
        setComp(GhostHouseAccessComp.class, new GhostHouseAccessComp());
        setComp(GhostStateComp.class, new GhostStateComp());
        setComp(GhostAnimationComp.class, new GhostAnimationComp());
        setComp(SpriteAnimationComp.class, new SpriteAnimationComp());
        setComp(RenderingComp.class, new RenderingComp(RenderingLayer.ACTORS));

        rendering().setLayerPriority(switch (personality) {
            case RED_GHOST_SHADOW   -> 13; // on top of all other ghosts
            case PINK_GHOST_SPEEDY  -> 12;
            case CYAN_GHOST_BASHFUL -> 11;
            case ORANGE_GHOST_POKEY -> 10; // behind all other ghosts
        });

        //TODO where does this belong?
        worldNavigation().corneringSpeedDelta = -1.25f;
    }

    public GhostPersonality personality() {
        return personality;
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

    public GhostHouseAccessComp houseAccess() {
        return reqComp(GhostHouseAccessComp.class);
    }

    public GhostStateComp state() {
        return reqComp(GhostStateComp.class);
    }

    public GhostAnimationComp animation() {
        return reqComp(GhostAnimationComp.class);
    }

    public SpriteAnimationComp spriteAnimation() {
        return reqComp(SpriteAnimationComp.class);
    }

    public RenderingComp rendering() {
        return reqComp(RenderingComp.class);
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