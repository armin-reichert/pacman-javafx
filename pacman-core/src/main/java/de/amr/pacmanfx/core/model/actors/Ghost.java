/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.comp.common.MovementComp;
import de.amr.pacmanfx.core.model.comp.ghost.GhostState;
import de.amr.pacmanfx.core.model.comp.ghost.GhostStateComp;
import de.amr.pacmanfx.core.model.comp.ghost.GhostWorldPlacementComp;
import de.amr.pacmanfx.core.model.comp.spriteanim.SpriteAnimComp;
import de.amr.pacmanfx.core.model.comp.world.WorldNavigationComp;

import static java.util.Objects.requireNonNull;

/**
 * A ghost. Ghosts differ in their personality which defines attack behavior and look.
 */
public class Ghost extends GameEntity implements UpdatableEntity {

    private final GhostPersonality personality;

    public Ghost(GhostPersonality personality, String name) {
        this.personality = requireNonNull(personality);
        this.name = requireNonNull(name);

        setComponent(MovementComp.class, new MovementComp());
        setComponent(WorldNavigationComp.class, new WorldNavigationComp());
        setComponent(GhostWorldPlacementComp.class, new GhostWorldPlacementComp());
        setComponent(GhostStateComp.class, new GhostStateComp());
        setComponent(SpriteAnimComp.class, new SpriteAnimComp());

        //TODO where does this belong?
        worldNavigation().corneringSpeedDelta = -1.25f;
    }

    public GhostPersonality personality() {
        return personality;
    }

    public MovementComp movement() {
        return requireComponent(MovementComp.class);
    }

    public GhostWorldPlacementComp worldPlacement() {
        return requireComponent(GhostWorldPlacementComp.class);
    }

    public WorldNavigationComp worldNavigation() {
        return requireComponent(WorldNavigationComp.class);
    }

    public GhostState state() {
        return requireComponent(GhostStateComp.class).state();
    }

    @Override
    public void update(GameContext gameContext) {
        gameContext.systems().ghostState().update(gameContext, this);
    }

    @Override
    public String toString() {
        return "Ghost{" +
            "personality=" + personality +
            ", state=" + state() +
            ", " + super.toString() +
            '}';
    }
}