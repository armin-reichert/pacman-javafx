/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public final class GameState_LevelTransition extends GameState {

    public GameState_LevelTransition() {
        super(CommonGameStateID.GAME_LEVEL_TRANSITION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        timer().restartSeconds(2);
        gameContext.gamePlay().startNextLevel(gameContext);
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.session().gameFlow().enterState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
