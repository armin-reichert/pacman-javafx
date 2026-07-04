/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

public class GameOverState extends GameState {

    public GameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        model.setPlaying(false);
        model.updateHighScore();
        model.lives().setCount(0); // Needed if state entry was triggered by user interaction
        model.showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        gameContext.cheats().clear();

        //TODO rethink this
        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        if (timer().hasExpired()) {
            level.clearMessage();

            final Identifier nextStateID = level.isDemoLevel()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : model.canContinueOnGameOver() ? GameStateID.GAME_PREPARATION : GameStateID.GAME_INTRO;

            gameContext.flow().enterState(nextStateID);
        }
    }
}
