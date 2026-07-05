/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;

public class CommonGameLevelTransitionState extends GameState{

    public CommonGameLevelTransitionState() {
        super(GameStateID.GAME_LEVEL_TRANSITION);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        timer().restartSeconds(2);
        context.gamePlay().startNextLevel(context.eventManager(), model.assertLevel());
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
