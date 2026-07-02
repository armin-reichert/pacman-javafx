/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.basics.Identifier;

/**
 * Enumeration of game state identifiers.
 *
 * <p>These constants exist to avoid scattering string literals throughout
 * game‑variant‑independent code. Implementations may define additional
 * states, but these represent the canonical set used across Pac‑Man FX.</p>
 */
public enum GameStateID implements Identifier {
    BOOT,
    GAME_INTRO,
    GAME_PREPARATION,
    GAME_STARTING,
    GAME_OR_LEVEL_STARTING,
    GAME_LEVEL_PLAYING,
    GAME_LEVEL_COMPLETE,
    GAME_LEVEL_CONTINUE,
    GAME_LEVEL_TRANSITION,
    GAME_LEVEL_EATING_GHOST,
    GAME_LEVEL_PACMAN_DYING,
    GAME_LEVEL_INTERMISSION,
    GAME_OVER,
    DEMO_LEVEL_PLAYING
}
