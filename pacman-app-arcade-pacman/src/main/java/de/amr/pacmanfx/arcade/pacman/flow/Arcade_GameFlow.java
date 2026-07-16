/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.event.GameStateChangeEvent;
import de.amr.pacmanfx.core.flow.GameFlow;
import org.tinylog.Logger;

/**
 * State machine with all Arcade game states.
 */
public class Arcade_GameFlow extends GameFlow {

    public Arcade_GameFlow() {
        super("Arcade Pac-Man Games Control Flow");
        for (Arcade_GameState gameState : Arcade_GameState.values()) {
            addState(gameState.state());
        }
        addStateChangeListener((oldState, newState) -> {
            if (gameContext != null) {
                gameContext.eventManager().publishGameEvent(new GameStateChangeEvent(gameContext, oldState, newState));
            } else {
                Logger.error("No game context existing when game event is fired");
            }
        });
    }
}
