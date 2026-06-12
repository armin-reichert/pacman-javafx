/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.Identifier;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;

import java.util.Objects;
import java.util.Optional;

public interface GameFlow {

    void setGameContext(GameContext gameContext);

    GameContext gameContext();

    Optional<State<GameContext>> optState(String stateName);

    GameState state();

    void addState(State<GameContext> gameState);

    void enterState(State<GameContext> gameState);

    default void enterState(Identifier id) {
        Objects.requireNonNull(id);
        enterState(id.name());
    }

    void enterState(String stateName);

    void resumePreviousState();

    void restartState(State<GameContext> gameState);

    void restartState(String stateName);

    default void restartState(GameStateID gameStateID) {
        restartState(gameStateID.name());
    }

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
