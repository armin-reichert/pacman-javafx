/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;

public class GameLevelTransitionState extends GameState{

    public GameLevelTransitionState() {
        super(GameStateID.GAME_LEVEL_TRANSITION);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel game = context.gameModel();
        timer().restartSeconds(2);
        game.startNextLevel();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.gameFlow().enterState(GameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
