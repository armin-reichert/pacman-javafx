/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public final class Common_LevelTransitionState extends GameState {

    public Common_LevelTransitionState() {
        super(CommonGameStateID.GAME_LEVEL_TRANSITION);
    }

    @Override
    public void onEnter(GameContext game) {
        timer().restartSeconds(2);
        game.variant().gamePlay().startNextLevel(game);
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
