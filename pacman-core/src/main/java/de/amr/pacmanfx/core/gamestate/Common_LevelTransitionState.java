/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public final class Common_LevelTransitionState extends AbstractGameState {

    public Common_LevelTransitionState() {
        super(CommonGameStateID.GAME_LEVEL_TRANSITION);
    }

    @Override
    public void doEnterState(GameContext game) {
        stateTimer().restartSeconds(2);
        game.play().startNextLevel(game);
    }

    @Override
    public void doUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        if (stateTimer().hasExpired()) {
            game.flow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
