/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.pacmanfx.core.GameContext;

public class CommonGameLevelTransitionState extends GameState {

    public CommonGameLevelTransitionState() {
        super(GameStateID.GAME_LEVEL_TRANSITION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        timer().restartSeconds(2);
        gameContext.gamePlay().startNextLevel(gameContext);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            gameContext.flow().enterState(gameContext, GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
