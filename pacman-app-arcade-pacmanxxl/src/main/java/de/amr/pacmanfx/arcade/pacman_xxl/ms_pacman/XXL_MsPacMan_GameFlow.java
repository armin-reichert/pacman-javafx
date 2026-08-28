package de.amr.pacmanfx.arcade.pacman_xxl.ms_pacman;

import de.amr.pacmanfx.arcade.pacman.gamestate.Arcade_GameState;
import de.amr.pacmanfx.core.gamestate.GameFlowController;

public class XXL_MsPacMan_GameFlow extends GameFlowController {

    public XXL_MsPacMan_GameFlow() {
        super("Arcade Ms. Pac-Man Game Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
    }
}
