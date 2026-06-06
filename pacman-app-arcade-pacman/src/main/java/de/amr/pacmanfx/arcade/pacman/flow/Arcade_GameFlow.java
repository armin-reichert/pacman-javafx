package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.flow.StateMachineGameFlow;

public class Arcade_GameFlow extends StateMachineGameFlow {

    public Arcade_GameFlow() {
        super("Arcade Pac-Man Games Control Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
    }
}
