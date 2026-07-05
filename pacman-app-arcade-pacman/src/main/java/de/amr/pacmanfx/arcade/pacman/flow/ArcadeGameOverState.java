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
import de.amr.pacmanfx.model.level.GameLevelMessageType;

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
    public void onUpdate(GameContext context) {
        final GameFlow flow = context.flow();
        final GameModel model = context.model();

        if (timer().hasExpired()) {
            final GameLevel level = context.model().assertLevel();
            level.clearMessage();
            context.cheats().clear();
            if (model.canStartNewGame(context)) {
                flow.enterState(GameStateID.GAME_PREPARATION);
            } else {
                flow.enterState(GameStateID.GAME_INTRO);
            }
        }
    }
}
