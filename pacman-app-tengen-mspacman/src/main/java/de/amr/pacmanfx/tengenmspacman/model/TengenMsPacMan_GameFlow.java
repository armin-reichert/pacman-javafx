/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;
import org.tinylog.Logger;

import java.util.Optional;

public class TengenMsPacMan_GameFlow implements GameFlow {

    private final StateMachine<Game> stateMachine = new StateMachine<>();

    public TengenMsPacMan_GameFlow(TengenMsPacMan_GameModel game) {
        stateMachine.setName("Tengen Ms. Pac-Man Game State Machine");
        stateMachine.addStates(TengenMsPacMan_GameState.values());
        stateMachine.setContext(game);
        stateMachine.addStateChangeListener((oldState, newState) -> game.publishGameEvent(
            new GameStateChangeEvent(game, oldState, newState)));
    }

    @Override
    public void updateState() {
        stateMachine.update();
    }

    @Override
    public void addState(State<Game> gameState) {
        stateMachine.addState(gameState);
    }

    @Override
    public Optional<State<Game>> optState(String stateName) {
        return stateMachine.optState(stateName);
    }

    @Override
    public State<Game> state() {
        return stateMachine.state();
    }

    @Override
    public void enterState(State<Game> gameState) {
        stateMachine.enterState(gameState);
    }

    @Override
    public void enterStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(stateMachine::enterState,
            () -> Logger.error("No game state named {} found", stateName));
    }

    @Override
    public void resumePreviousState() {
        stateMachine.resumePreviousState();
    }

    @Override
    public void restartState(State<Game> gameState) {
        stateMachine.restart(gameState);
    }

    @Override
    public void restartStateWithName(String stateName) {
        optState(stateName).ifPresentOrElse(stateMachine::restart,
            () -> Logger.error("No game state named {} found", stateName));

    }
}
