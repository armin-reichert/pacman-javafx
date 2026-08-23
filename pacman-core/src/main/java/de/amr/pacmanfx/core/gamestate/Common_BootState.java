/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

/**
 * Corresponds to the screen showing all these random symbols from the Arcade video memory.
 */
public final class Common_BootState extends GameState {

    public interface Timing {
        int HEX_CODES      = 60;
        int SPRITE_GARBAGE = 120;
        int GRID           = 210;
        int EXPIRATION     = 240;
    }

    public Common_BootState() {
        // "Das muss das Boot abkönnen! Jawohl, Herr Kaleu!"
        super(CommonGameStateID.BOOT);
    }

    @Override
    public void onEnter(GameContext game) {
        timer().restartTicks(Timing.EXPIRATION);
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_INTRO);
        }
    }
}
