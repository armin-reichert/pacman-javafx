/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;

public class Arcade_GameFlow implements GameFlow {

    final StateMachine<Game> stateMachine = new StateMachine<>();

    public Arcade_GameFlow(Arcade_GameModel game) {
        stateMachine.setName("Arcade Pac-Man (common) Game Control");
        stateMachine.setContext(game);
        stateMachine.addStateChangeListener((oldState, newState) -> game.publishGameEvent(new GameStateChangeEvent(game, oldState, newState)));
        stateMachine.addStates(Arcade_GameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return stateMachine;
    }
}
