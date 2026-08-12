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
        gameContext.session().hud().showLevelCounter().hideLivesCounter().showCredit().showScore().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.session().gameFlow().enterState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
