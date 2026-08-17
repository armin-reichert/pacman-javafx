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
    public void onEnter(GameContext game) {
        game.session().hud().showLevelCounter().hideLivesCounter().showCredit().showScore().show();
        game.session().setLevel(null);
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
