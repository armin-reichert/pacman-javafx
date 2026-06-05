/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.gamestate.GameStateIdentifier;

import java.util.Objects;
import java.util.Optional;

public interface GameFlow {

    GameContext context();

    Optional<State<GameContext>> optState(String stateName);

    State<GameContext> state();

    void addState(State<GameContext> gameState);

    void enterState(State<GameContext> gameState);

    default void enterState(GameStateIdentifier id) {
        Objects.requireNonNull(id);
        enterState(id.name());
    }

    void enterState(String stateName);

    void resumePreviousState();

    void restartState(State<GameContext> gameState);

    void restartState(String stateName);

    void makeStep();

    // Game events

    void addGameEventListener(GameEventListener listener);

    void removeGameEventListener(GameEventListener listener);

    void publishGameEvent(GameEvent event);

    // Cut scenes

    /** @return {@code true} if cut scenes are enabled */
    boolean cutScenesEnabled();

    /** Enables or disables cut scenes. */
    void setCutScenesEnabled(boolean enabled);
}
