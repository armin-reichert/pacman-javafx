/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;

public class ArcadeGameOverState extends GameState {

    public ArcadeGameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();

        timer().restartTicks(level.gameOverStateTicks());
        //TODO check if this is needed
        gameContext.model().lives().setCount(0);
        gameModel.onGameOver(gameContext, level);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();

        if (timer().hasExpired()) {
            final GameLevel level = gameContext.requireLevel();
            level.clearMessage();
            gameContext.cheats().clear();
            if (gameModel.canStartNewGame(gameContext)) {
                flow.enterState(GameStateID.GAME_PREPARATION);
            } else {
                flow.enterState(GameStateID.GAME_INTRO);
            }
        }
    }

}
