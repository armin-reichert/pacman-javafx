package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.StateMachineGameFlow;

public class Arcade_GameFlow extends StateMachineGameFlow {

    public Arcade_GameFlow(GameContext context) {
        super("Arcade Pac-Man Games Control Flow", context);
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
    }
}
