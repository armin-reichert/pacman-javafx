/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.model.GameModel;

import java.util.Optional;

public interface GameControlFlow {

    Optional<State<GameModel>> optState(String stateName);

    State<GameModel> state();

    void addState(State<GameModel> gameState);

    void enterState(State<GameModel> gameState);

    void enterState(String stateName);

    void resumePreviousState();

    void restartState(State<GameModel> gameState);

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
