/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model;

import de.amr.pacmanfx.lib.fsm.State;

import java.util.Optional;

public interface GameFlow {

    /**
     * Enumeration of game state identifiers.
     *
     * <p>These constants exist to avoid scattering string literals throughout
     * game‑variant‑independent code. Implementations may define additional
     * states, but these represent the canonical set used across Pac‑Man FX.</p>
     */
    enum CanonicalGameState {
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
        INTERMISSION
    }

    void updateState();

    Optional<State<Game>> optState(String stateName);

    State<Game> state();

    void addState(State<Game> gameState);

    void enterState(State<Game> gameState);

    void enterStateWithName(String stateName);

    void resumePreviousState();

    void restartState(State<Game> gameState);

    void restartStateWithName(String stateName);
}
