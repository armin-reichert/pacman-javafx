/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import org.tinylog.Logger;

public class Globals {

    // Ghost personalities
    public static final byte RED_GHOST_SHADOW = 0, PINK_GHOST_SPEEDY = 1, CYAN_GHOST_BASHFUL = 2, ORANGE_GHOST_POKEY = 3;

    public static final byte NUM_TICKS_PER_SEC = 60;

    /** Tile size: 8px. */
    public static final byte TS = 8;

    /** Half tile size: 4px. */
    public static final byte HTS = 4;

    private static GameContextImpl THE_GAME_CONTEXT;

    public static GameContext theGameContext() {
        if (THE_GAME_CONTEXT == null) {
            THE_GAME_CONTEXT = new GameContextImpl();
            Logger.info("Global game context created!");
        }
        return THE_GAME_CONTEXT;
    }
}