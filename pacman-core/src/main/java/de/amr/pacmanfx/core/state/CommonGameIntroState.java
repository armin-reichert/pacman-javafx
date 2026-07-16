/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.pacmanfx.core.GameContext;

public class CommonGameIntroState extends GameState {

    public CommonGameIntroState() {
        super(GameStateID.GAME_INTRO);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.gamePlay().resetForNewGame(gameContext);
        gameContext.model().hudState().showLevelCounter().hideLivesCounter().showCredit().showScore().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(context, GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
