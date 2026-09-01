/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

/**
 * Corresponds to the screen showing all these random symbols from the Arcade video memory.
 */
public final class Common_BootState extends AbstractGameState {

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
    public void onEnterState(GameContext game) {
        timer().restartTicks(Timing.EXPIRATION);
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (timer().hasExpired()) {
            flow.enterGameState(game, CommonGameStateID.GAME_INTRO);
        }
    }
}
