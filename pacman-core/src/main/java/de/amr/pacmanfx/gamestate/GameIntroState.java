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
        context.model().resetForNewGame();
        context.model().hud().levelCounterOn().livesCounterOff().creditOn().scoreOn().show();
        lock();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
