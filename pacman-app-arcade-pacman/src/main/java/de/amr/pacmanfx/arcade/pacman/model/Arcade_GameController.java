/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;

public class Arcade_GameController extends StateMachine<Game> implements GameControl {

    public Arcade_GameController() {
        setName("Arcade Pac-Man Games State Machine");
        addStates(Arcade_GameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return this;
    }

}