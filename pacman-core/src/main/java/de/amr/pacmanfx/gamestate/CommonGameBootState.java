/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;

/**
 * Corresponds to the screen showing all these random symbols from the Arcade video memory.
 */
public class CommonGameBootState extends GameState {

    public interface Timing {
        int HEX_CODES      = 60;
        int SPRITE_GARBAGE = 120;
        int GRID           = 210;
        int EXPIRATION     = 240;
    }

    public CommonGameBootState() {
        // "Das muss das Boot abkönnen! Jawohl, Herr Kaleu!"
        super(GameStateID.BOOT);
    }

    @Override
    public void onEnter(GameContext context) {
        timer().restartTicks(Timing.EXPIRATION);
        context.gamePlay().init(context);
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(GameStateID.GAME_INTRO);
        }
    }
}
