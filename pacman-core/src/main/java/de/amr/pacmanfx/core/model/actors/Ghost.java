/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.actors;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.UpdatableEntity;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.component.ghost.Elroy;
import de.amr.pacmanfx.core.model.component.ghost.GhostStateComponent;
import de.amr.pacmanfx.core.model.component.ghost.GhostWorldPlacement;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;

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

    /**
     * Notifies this ghost about Pac-Man's assassination so he can react accordingly (send condolence message etc.)
     * @param ignored the game level where this happens
     */
    public void onPacKilled(GameLevel ignored) {
        if (hasComponent(Elroy.class)) {
            assertComponent(Elroy.class).setEnabled(false);
        }
    }

    //TODO move into ghost sprite animation system
    public void playFrightenedAnimation(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.assertLevel();
        final Pac pac = level.entities().pac();

        if (sys.pacPower().isPowerStartingFading(level, pac)) {
            sys.spriteAnim().select(this, CommonAnimationID.GHOST_FLASHING);
            sys.spriteAnim().playSelected(this);
        }
        else if (!sys.pacPower().isPowerFading(level, pac)) {
            sys.spriteAnim().select(this, CommonAnimationID.GHOST_FRIGHTENED);
            sys.spriteAnim().playSelected(this);
        }
    }
}