/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;

public class GameBootState extends GameState {

    public interface Timing {
        int HEX_CODES      = 60;
        int SPRITE_GARBAGE = 120;
        int GRID           = 210;
        int EXPIRATION     = 240;
    }

    public GameBootState() {
        // "Das muss das Boot abkönnen! Jawohl, Herr Kaleu!"
        super(GameStateID.BOOT);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        timer().restartTicks(Timing.EXPIRATION);
        gameContext.model().init();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            gameContext.flow().enterState(GameStateID.GAME_INTRO);
        }
    }
}
