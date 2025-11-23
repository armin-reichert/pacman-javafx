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

public class GamePlayStateMachine extends StateMachine<FsmState<GameContext>, GameContext> implements GameEventManager {

    private final Game game;

    public GamePlayStateMachine(GameContext gameContext, Game game) {
        super(gameContext);

        this.game = requireNonNull(game);
        List<FsmState<GameContext>> states = new ArrayList<>(List.of(GamePlayState.values()));
        states.add(new LevelShortTestState());
        states.add(new LevelMediumTestState());
        states.add(new CutScenesTestState());
        setStates(states);
        setName("Game Controller State Machine");
        addStateChangeListener((oldState, newState) -> publishEvent(new GameStateChangeEvent(game, oldState, newState)));
    }

    public FsmState<GameContext> stateByName(String name) {
        return states().stream()
            .filter(state -> state.name().equals(name))
            .findFirst().orElseThrow();
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
        publishEvent(new GameEvent(game, type));
    }

    @Override
    public void publishEvent(GameEventType type, Vector2i tile) {
        requireNonNull(type);
        publishEvent(new GameEvent(game, type, tile));
    }
}
