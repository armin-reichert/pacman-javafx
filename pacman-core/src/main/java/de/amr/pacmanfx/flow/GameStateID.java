/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.model.GameModel;

import static java.util.Objects.requireNonNull;

/**
 * Enumeration of game state identifiers.
 *
 * <p>These constants exist to avoid scattering string literals throughout
 * game‑variant‑independent code. Implementations may define additional
 * states, but these represent the canonical set used across Pac‑Man FX.</p>
 */
public enum GameStateID {
    BOOT,
    INTRO,
    PREPARING_GAME_START,
    STARTING_GAME_OR_LEVEL,
    LEVEL_PLAYING,
    LEVEL_COMPLETE,
    LEVEL_TRANSITION,
    EATING_GHOST,
    PACMAN_DYING,
    GAME_OVER,
    INTERMISSION;

    public boolean identifies(State<GameModel> gameState) {
        requireNonNull(gameState);
        return gameState.name().equals(name());
    }
}
