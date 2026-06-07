/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;

public class GameIntroState extends GameState {

    public GameIntroState() {
        super(GameStateID.GAME_INTRO);
    }

    @Override
    public void onEnter(GameContext context) {
        context.gameModel().hud().levelCounterOn().livesCounterOff().creditOn().scoreOn().show();
        lock();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.gameFlow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
