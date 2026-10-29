/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.basics.ui.entities.props.messageview.MessageType;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;

import java.io.IOException;

public class Arcade_GameOverState extends AbstractGameState {

    public Arcade_GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void onEnterState(GameContext game) {
        final GameLevel level = session.optLevel().orElseThrow();

        level.showMessage(MessageType.GAME_OVER);
        session.hud().creditDisplay().show();
        session.store(SessionKey.GAME_RUNNING, false);
        session.cheats().clear();
        try {
            systems.scoreSystem().saveHighScoreIfNeeded(hud.highScore());
        } catch (IOException e) {
            game.runtime().eventManager().publishEvent(new HighScoreAccessErrorEvent(e));
        }
        timer().restartTicks(session.value(SessionKey.GAME_OVER_STATE_TICKS, Long.class));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (timer().hasExpired()) {
            final GameLevel level = session.optLevel().orElseThrow();
            level.entitySet().theOne(MessageView.class).hide();
            session.cheats().clear();
            session.removeLevel();
            flow.enterGameState(game, game.coinMechanism().isEmpty()
                ? CommonGameStateID.GAME_INTRO
                : CommonGameStateID.GAME_PREPARATION);
        }
    }
}
