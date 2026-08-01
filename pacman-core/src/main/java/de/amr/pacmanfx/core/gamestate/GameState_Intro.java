/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public final class GameState_Intro extends GameState {

    public GameState_Intro() {
        super(CommonGameStateID.GAME_INTRO);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.gamePlay().resetForNewGame(gameContext);
        gameContext.hudState().showLevelCounter().hideLivesCounter().showCredit().showScore().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            gameContext.flow().enterState(gameContext, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
