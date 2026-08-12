/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;

import java.io.IOException;

public class GameOverState extends GameState {

    public GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();

        LivesCounterSystem.setNumLives(session.livesCounter(), 0); // Needed if state entry was triggered by user interaction
        session.setPlaying(false);

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gamePlay.showLevelMessage(game, level, GameLevelMessageType.GAME_OVER);

        game.cheats().clear();

        //TODO rethink this
        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext game) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        if (timer().hasExpired()) {
            level.clearMessage();
            final Named nextStateID = session.isAttractMode()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : gamePlay.canContinueOnGameOver(session) ? CommonGameStateID.GAME_PREPARATION : CommonGameStateID.GAME_INTRO;

            game.session().gameFlow().enterState(game, nextStateID);
        }
    }
}
