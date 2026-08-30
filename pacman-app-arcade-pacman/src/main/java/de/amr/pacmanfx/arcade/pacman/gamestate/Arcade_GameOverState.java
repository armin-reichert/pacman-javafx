/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.MessageType;

import java.io.IOException;

public class Arcade_GameOverState extends AbstractGameState {

    public Arcade_GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnterState(GameContext game) {
        try {
            systems.scoreSystem().saveHighScoreIfNeeded(hud.highScore());
        } catch (IOException e) {
            game.eventManager().publishGameEvent(new HighScoreAccessErrorEvent(e));
        }

        gamePlay.showMessage(game, MessageType.GAME_OVER);

        session.setGameRunning(false);
        session.cheats().clear();

        timer().restartTicks(session.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            session.hud().clearMessage();
            session.cheats().clear();
            flow.enterGameState(game, game.coinMechanism().isEmpty()
                ? CommonGameStateID.GAME_INTRO
                : CommonGameStateID.GAME_PREPARATION);
        }
    }
}
