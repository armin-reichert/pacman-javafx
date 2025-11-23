/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.test.CutScenesTestState;
import de.amr.pacmanfx.controller.test.LevelMediumTestState;
import de.amr.pacmanfx.controller.test.LevelShortTestState;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class GamePlayStateMachine implements GameEventManager {

    private final StateMachine<FsmState<GameContext>, GameContext> stateMachine;
    private final GameContext gameContext;

    public GamePlayStateMachine(GameContext gameContext) {
        this.gameContext = requireNonNull(gameContext);
        List<FsmState<GameContext>> states = new ArrayList<>(List.of(GamePlayState.values()));
        states.add(new LevelShortTestState());
        states.add(new LevelMediumTestState());
        states.add(new CutScenesTestState());
        stateMachine = new StateMachine<>(states, gameContext);
        stateMachine.setName("Game Controller State Machine");
        stateMachine.addStateChangeListener(
            (oldState, newState) -> publishEvent(new GameStateChangeEvent(gameContext.game(), oldState, newState)));
    }

    public void update() {
        stateMachine.update();
    }

    public void letCurrentGameStateExpire() {
        stateMachine.letCurrentStateExpire();
    }

    public void resumePreviousGameState() {
        stateMachine.resumePreviousState();
    }

    public void restart(FsmState<GameContext> state) {
        stateMachine.restart(state);
    }

    public void changeGameState(FsmState<GameContext> state) {
        requireNonNull(state);
        stateMachine.changeState(state);
    }

    public FsmState<GameContext> stateByName(String name) {
        return stateMachine.states().stream()
                .filter(state -> state.name().equals(name))
                .findFirst().orElseThrow();
    }

    public FsmState<GameContext> state() {
        return stateMachine.state();
    }

    // GameEventManager implementation

    private final List<GameEventListener> eventListeners = new ArrayList<>();

    @Override
    public void addEventListener(GameEventListener listener) {
        requireNonNull(listener);
        if (!eventListeners.contains(listener)) {
            eventListeners.add(listener);
            Logger.info("{}: Game event listener registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void removeEventListener(GameEventListener listener) {
        requireNonNull(listener);
        boolean removed = eventListeners.remove(listener);
        if (removed) {
            Logger.info("{}: Game event listener removed: {}", getClass().getSimpleName(), listener);
        } else {
            Logger.warn("{}: Game event listener not removed, as not registered: {}", getClass().getSimpleName(), listener);
        }
    }

    @Override
    public void publishEvent(GameEvent event) {
        requireNonNull(event);
        eventListeners.forEach(subscriber -> subscriber.onGameEvent(event));
        Logger.trace("Published game event: {}", event);
    }

    @Override
    public void publishEvent(GameEventType type) {
        requireNonNull(type);
        publishEvent(new GameEvent(gameContext.game(), type));
    }

    @Override
    public void publishEvent(GameEventType type, Vector2i tile) {
        requireNonNull(type);
        publishEvent(new GameEvent(gameContext.game(), type, tile));
    }
}
