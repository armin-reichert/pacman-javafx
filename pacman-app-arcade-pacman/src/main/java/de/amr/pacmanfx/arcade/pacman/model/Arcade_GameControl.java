/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.model;

import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;

public class Arcade_GameControl implements GameControl {

    private final StateMachine<Game> stateMachine = new StateMachine<>();

    public Arcade_GameControl() {
        stateMachine.setName("Arcade Pac-Man (all variants) Game Control");
        stateMachine.addStates(Arcade_GameState.values());
    }

    @Override
    public StateMachine<Game> stateMachine() {
        return stateMachine;
    }
}