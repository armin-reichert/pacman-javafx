/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

import java.io.IOException;

public class GameOverState extends GameState {

    public GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameSession session = gameContext.session();
        final GameLevel level = session.assertLevel();
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) gameContext.gamePlay();

        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        LivesCounterSystem.setNumLives(livesCounter, 0); // Needed if state entry was triggered by user interaction

        session.setPlaying(false);

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gamePlay.showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        gameContext.cheats().clear();

        //TODO rethink this
        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) game.model();
        final GameLevel level = session.assertLevel();

        if (timer().hasExpired()) {
            level.clearMessage();

            final Named nextStateID = session.isDemoLevel()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : model.canContinueOnGameOver() ? CommonGameStateID.GAME_PREPARATION : CommonGameStateID.GAME_INTRO;

            game.flow().enterState(game, nextStateID);
        }
    }
}
