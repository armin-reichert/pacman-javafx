/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameEntity;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.MovementComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostState;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacementComponent;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnimComponent;
import de.amr.pacmanfx.core.model.component.world.WorldNavigationComponent;

import static java.util.Objects.requireNonNull;

/**
 * A ghost. Ghosts differ in their personality which defines attack behavior and look.
 */
public class Ghost extends GameEntity implements UpdatableEntity {

    private final GhostPersonality personality;

    public Ghost(GhostPersonality personality, String name) {
        this.personality = requireNonNull(personality);
        this.name = requireNonNull(name);

        setComponent(MovementComponent.class, new MovementComponent());
        setComponent(WorldNavigationComponent.class, new WorldNavigationComponent());
        setComponent(GhostWorldPlacementComponent.class, new GhostWorldPlacementComponent());
        setComponent(GhostStateComponent.class, new GhostStateComponent());
        setComponent(SpriteAnimComponent.class, new SpriteAnimComponent());

        //TODO where does this belong?
        worldNavigation().corneringSpeedDelta = -1.25f;
    }

    public GhostPersonality personality() {
        return personality;
    }

    public MovementComponent movement() {
        return requireComponent(MovementComponent.class);
    }

    public GhostWorldPlacementComponent worldPlacement() {
        return requireComponent(GhostWorldPlacementComponent.class);
    }

    public WorldNavigationComponent worldNavigation() {
        return requireComponent(WorldNavigationComponent.class);
    }

    public GhostState state() {
        return requireComponent(GhostStateComponent.class).state();
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