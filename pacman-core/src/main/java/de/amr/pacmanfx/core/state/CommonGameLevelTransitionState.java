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
    public void onEnter(GameContext context) {
        timer().restartSeconds(2);
        context.gamePlay().startNextLevel(context.createPlayContext());
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
