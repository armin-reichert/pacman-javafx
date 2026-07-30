/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacement;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;

import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public class Ghost extends GameEntity implements UpdatableEntity {

    private final GhostPersonality personality;

    public Ghost(GhostPersonality personality, String name) {
        this.personality = requireNonNull(personality);
        this.name = requireNonNull(name);
    }

    /**
     * @return this ghost's personality, see {@link GhostPersonality#RED_GHOST_SHADOW},
     * {@link GhostPersonality#PINK_GHOST_SPEEDY}, {@link GhostPersonality#CYAN_GHOST_BASHFUL} and
     * {@link GhostPersonality#ORANGE_GHOST_POKEY}.
     */
    public GhostPersonality personality() {
        return personality;
    }

    public Movement movement() {
        return requireComponent(Movement.class);
    }

    public GhostWorldPlacement worldPlacement() {
        return requireComponent(GhostWorldPlacement.class);
    }

    public WorldNavigation worldNavigation() {
        return requireComponent(WorldNavigation.class);
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