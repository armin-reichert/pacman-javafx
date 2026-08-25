/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.LevelMessageType;

import java.io.IOException;

public class Arcade_GameOverState extends GameState {

    public Arcade_GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.level();

        try {
            ScoreSystem.saveHighScoreIfNeeded(session.hud().highScore());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        game.variant().gamePlay().showLevelMessage(game, level, LevelMessageType.GAME_OVER);
        session.setGameRunning(false);
        game.session().cheats().clear();

        timer().restartTicks(session.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();

        systems.entityUpdater().updateHUD(game);

        if (timer().hasExpired()) {
            session.hud().clearMessage();
            session.cheats().clear();
            game.variant().gameFlow().enterGameState(game, game.coinMechanism().isEmpty()
                ? CommonGameStateID.GAME_INTRO
                : CommonGameStateID.GAME_PREPARATION);
        }
    }
}
