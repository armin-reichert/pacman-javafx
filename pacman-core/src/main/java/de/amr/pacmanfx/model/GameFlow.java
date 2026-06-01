/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameEventListener;

import java.util.Optional;

public interface GameFlow {

    Optional<State<Game>> optState(String stateName);

    State<Game> state();

    void addState(State<Game> gameState);

    void enterState(State<Game> gameState);

    void enterStateWithName(String stateName);

    void resumePreviousState();

    void restartState(State<Game> gameState);

    void restartState(String stateName);

    void update();

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
