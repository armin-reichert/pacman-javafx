/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;

public class TengenMsPacMan_GameController extends StateMachine<Game> implements GameControl {

    public TengenMsPacMan_GameController() {
        setName("Tengen Ms. Pac-Man Game State Machine");
        addStates(TengenGameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return this;
    }

}