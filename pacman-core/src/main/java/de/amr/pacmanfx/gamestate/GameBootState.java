/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;

public class GameBootState extends GameState {

    public GameBootState() {
        // "Das muss das Boot abkönnen! Jawohl, Herr Kaleu!"
        super(GameStateID.BOOT);
    }


    @Override
    public void onEnter(GameContext context) {
        final GameModel game = context.gameModel();
        lock();
        game.init();
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel game = context.gameModel();
        if (timer().hasExpired()) {
            game.flow().enterState(GameStateID.GAME_INTRO);
        }
    }
}
