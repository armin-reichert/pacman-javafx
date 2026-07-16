/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.flow.GameFlowController;

/**
 * State machine with all Arcade game states.
 */
public class Arcade_GameFlowController extends GameFlowController {

    public Arcade_GameFlowController() {
        super("Arcade Pac-Man Games Control Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
    }
}
