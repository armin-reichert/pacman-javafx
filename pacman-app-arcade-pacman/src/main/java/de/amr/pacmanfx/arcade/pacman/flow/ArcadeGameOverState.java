/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameOverState extends GameState {

    public ArcadeGameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = context.model().assertLevel();

        context.gamePlay().updateHighScore(context);
        context.gamePlay().showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        // In case, entering game over state was forced by user:
        model.lives().setCount(0);
        model.setPlaying(false);

        context.cheats().clear();

        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController flow = gameContext.flow();
        if (timer().hasExpired()) {
            final GameLevel level = gameContext.model().assertLevel();
            level.clearMessage();
            gameContext.cheats().clear();
            flow.enterState(gameContext, gameContext.coinMechanism().isEmpty()
                ? GameStateID.GAME_INTRO
                : GameStateID.GAME_PREPARATION);
        }
    }
}
