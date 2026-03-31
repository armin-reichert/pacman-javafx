/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;

public class TengenMsPacMan_GameControl implements GameControl {

    private final StateMachine<Game> stateMachine = new StateMachine<>();

    public TengenMsPacMan_GameControl() {
        stateMachine.setName("Tengen Ms. Pac-Man Game State Machine");
        stateMachine.addStates(TengenGameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return stateMachine;
    }
}