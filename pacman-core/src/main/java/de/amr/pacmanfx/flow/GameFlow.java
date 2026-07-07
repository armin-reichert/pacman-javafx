/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;

import java.util.Objects;
import java.util.Optional;

public interface GameFlow {

    void setContext(GameContext context);

    GameContext context();

    Optional<State<GameContext>> optState(Identifier stateID);

    GameState state();

    void addState(State<GameContext> gameState);

    void enterState(State<GameContext> gameState);

    default void enterState(Identifier id) {
        Objects.requireNonNull(id);
        enterStateWithName(id.name());
    }

    void enterStateWithName(String stateName);

    void resumePreviousState();

    void restartState(State<GameContext> gameState);

    void restartState(String stateName);

    default void restartState(Identifier stateID) {
        restartState(stateID.name());
    }

    void makeStep();

    /** @return {@code true} if cut scenes are enabled */
    boolean cutScenesEnabled();

    /** Enables or disables cut scenes. */
    void setCutScenesEnabled(boolean enabled);
}
