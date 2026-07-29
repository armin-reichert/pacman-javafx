/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacement;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;

import java.util.Collection;

import static de.amr.pacmanfx.core.Validations.requireValidGhostPersonality;
import static de.amr.pacmanfx.core.Validations.stateIsOneOf;
import static java.util.Objects.requireNonNull;

/**
 * Common ghost base class. The specific ghosts differ in their hunting behavior and their look.
 */
public class Ghost extends Actor implements UpdatableEntity {

    private final byte personality;

    public Ghost(byte personality, String name) {
        this.personality = requireValidGhostPersonality(personality);
        this.name = requireNonNull(name);
    }

    /**
     * @return this ghost's personality, see {@link GameModel#RED_GHOST_SHADOW},
     * {@link GameModel#PINK_GHOST_SPEEDY}, {@link GameModel#CYAN_GHOST_BASHFUL} and
     * {@link GameModel#ORANGE_GHOST_POKEY}.
     */
    public byte personality() {
        return personality;
    }

    public Movement movement() {
        return assertComponent(Movement.class);
    }

    public GhostWorldPlacement worldPlacement() {
        return assertComponent(GhostWorldPlacement.class);
    }

    public WorldNavigation worldNavigation() {
        return assertComponent(WorldNavigation.class);
    }

    public GhostState state() {
        return assertComponent(GhostStateComponent.class).state();
    }

    /**
     * @param states ghost states to be checked
     * @return <code>true</code> if the ghost is in any of the given states.
     * If no alternatives are given, an exception is thrown.
     * <code>false</code>
     */
    public boolean inAnyOfStates(Collection<GhostState> states) {
        return stateIsOneOf(state(), states);
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
            ", worldPlacement=" + worldPlacement() +
            ", worldNavigation=" + worldNavigation() +
            ", " + super.toString() +
            '}';
    }
}