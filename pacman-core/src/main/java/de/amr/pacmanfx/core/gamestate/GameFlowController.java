/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.pacmanfx.core.GameContext;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * A game flow implementation using a state machine.
 */
public class GameFlowController extends StateMachine<GameContext> {

    public GameFlowController(String name) {
        setName(name);
    }

    @Override
    public AbstractGameState state() {
        return (AbstractGameState) super.state();
    }

    public void enterGameState(GameContext game, Named gameStateID) {
        requireNonNull(game);
        requireNonNull(gameStateID);

        enterStateWithName(game, gameStateID.name());
    }

    public void restartGameState(GameContext game, Named gameStateID) {
        requireNonNull(game);
        requireNonNull(gameStateID);

        restartState(game, gameStateID.name());
    }

    public Optional<State<GameContext>> optGameState(Named gameStateID) {
        requireNonNull(gameStateID);

        return optState(gameStateID.name());
    }
}
