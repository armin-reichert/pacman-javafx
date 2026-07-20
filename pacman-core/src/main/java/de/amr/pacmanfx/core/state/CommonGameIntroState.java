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
        gameContext.hudState().showLevelCounter().hideLivesCounter().showCredit().showScore().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            gameContext.flow().enterState(gameContext, GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
