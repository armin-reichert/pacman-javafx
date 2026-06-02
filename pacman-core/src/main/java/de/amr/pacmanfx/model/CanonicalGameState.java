/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model;

import de.amr.basics.fsm.State;

import static java.util.Objects.requireNonNull;

/**
 * Enumeration of game state identifiers.
 *
 * <p>These constants exist to avoid scattering string literals throughout
 * game‑variant‑independent code. Implementations may define additional
 * states, but these represent the canonical set used across Pac‑Man FX.</p>
 */
public enum CanonicalGameState {
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

    public boolean matches(State<GameModel> gameState) {
        requireNonNull(gameState);
        return gameState.matchesByName(name());
    }
}
