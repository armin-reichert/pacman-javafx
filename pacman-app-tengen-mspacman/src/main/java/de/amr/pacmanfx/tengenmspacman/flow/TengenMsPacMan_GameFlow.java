package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.StateMachineGameControlFlow;

public class TengenMsPacMan_GameFlow extends StateMachineGameControlFlow {

    public TengenMsPacMan_GameFlow(GameContext context) {
        super("Tengen Ms. Pac-Man Game Flow", context);
        for (TengenMsPacMan_GameState gameState : TengenMsPacMan_GameState.values()) {
            addState(gameState.state());
        }
    }
}
