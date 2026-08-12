/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
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
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        game.gamePlay().showLevelMessage(game, level, GameLevelMessageType.GAME_OVER);

        // In case, entering game over state was forced by user:
        LivesCounterSystem.setNumLives(session.livesCounter(), 0);
        session.setPlaying(false);

        game.cheats().clear();

        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            final GameLevel level = game.session().assertLevel();
            level.clearMessage();
            game.cheats().clear();
            game.session().gameFlow().enterState(game, game.coinMechanism().isEmpty()
                ? CommonGameStateID.GAME_INTRO
                : CommonGameStateID.GAME_PREPARATION);
        }
    }
}
