package de.amr.pacmanfx.arcade.pacman_xxl.pacman;

import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.core.gamestate.GameFlowController;

public class XXL_PacMan_GameFlow extends GameFlowController {

    public XXL_PacMan_GameFlow() {
        super("Arcade Pac-Man XXL Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
    }
}
