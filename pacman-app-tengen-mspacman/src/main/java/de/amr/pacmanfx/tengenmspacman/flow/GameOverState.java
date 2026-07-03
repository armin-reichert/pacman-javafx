/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;

public class GameOverState extends GameState {

    public GameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        // In case, entering game over state was forced by user:
        gameContext.model().lives().setCount(0);
        timer().restartTicks(level.gameOverStateTicks());
        gameModel.onGameOver(gameContext, level);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        if (timer().hasExpired()) {
            gameContext.cheats().clear();
            level.clearMessage();

            if (level.isDemoLevel()) {
                flow.enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
            }
            else {
                flow.enterState(gameModel.canContinueOnGameOver() ? GameStateID.GAME_PREPARATION : GameStateID.GAME_INTRO);
            }
        }
    }

}
