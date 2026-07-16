package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.flow.GameFlowController;

public class TengenMsPacMan_GameFlowController extends GameFlowController {

    public TengenMsPacMan_GameFlowController() {
        super("Tengen Ms. Pac-Man Game Flow");
        for (TengenMsPacMan_GameState gameState : TengenMsPacMan_GameState.values()) {
            addState(gameState.state());
        }
    }
}
