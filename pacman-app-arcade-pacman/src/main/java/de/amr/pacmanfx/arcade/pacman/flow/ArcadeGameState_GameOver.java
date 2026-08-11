/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.livescounter.system.LivesCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.session.GameSession;

import java.io.IOException;

public class ArcadeGameState_GameOver extends GameState {

    public ArcadeGameState_GameOver() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameSession session = gameContext.session();
        final GameLevel level = session.assertLevel();

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gameContext.gamePlay().showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        // In case, entering game over state was forced by user:
        final LivesCounter livesCounter = level.entities().theOne(LivesCounter.class);
        LivesCounterSystem.setNumLives(livesCounter, 0);
        session.setPlaying(false);

        gameContext.cheats().clear();

        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            final GameLevel level = gameContext.session().assertLevel();
            level.clearMessage();
            gameContext.cheats().clear();
            gameContext.flow().enterState(gameContext, gameContext.coinMechanism().isEmpty()
                ? CommonGameStateID.GAME_INTRO
                : CommonGameStateID.GAME_PREPARATION);
        }
    }
}
