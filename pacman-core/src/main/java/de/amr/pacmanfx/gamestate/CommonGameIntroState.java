/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;

public class CommonGameIntroState extends GameState {

    public CommonGameIntroState() {
        super(GameStateID.GAME_INTRO);
    }

    @Override
    public void onEnter(GameContext context) {
        context.gamePlay().resetForNewGame(context.model());
        context.model().hudState().levelCounterOn().livesCounterOff().creditOn().scoreOn().showIt();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
