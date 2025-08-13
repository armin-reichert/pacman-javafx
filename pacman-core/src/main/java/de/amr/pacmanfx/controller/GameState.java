package de.amr.pacmanfx.controller;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.fsm.FsmState;

public interface GameState extends FsmState<GameContext> {
    String name();

    default boolean is(Class<? extends GameState> stateClass) {
        return name().equals(stateClass.getSimpleName());
    }
}
