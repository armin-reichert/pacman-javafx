/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

public class GameOverState extends GameState {

    public GameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();

        model.setPlaying(false);
        model.lives().setCount(0); // Needed if state entry was triggered by user interaction

        context.gamePlay().updateHighScore(context.createPlayContext());
        context.gamePlay().showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        context.cheats().clear();

        //TODO rethink this
        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext context) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) context.model();
        final GameLevel level = model.assertLevel();

        if (timer().hasExpired()) {
            level.clearMessage();

            final Identifier nextStateID = level.isDemoLevel()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : model.canContinueOnGameOver() ? GameStateID.GAME_PREPARATION : GameStateID.GAME_INTRO;

            context.flow().enterState(nextStateID);
        }
    }
}
