/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;

public final class Common_IntroState extends AbstractGameState {

    public Common_IntroState() {
        super(CommonGameStateID.GAME_INTRO);
    }

    @Override
    public void onEnterState(GameContext game) {
        game.session().hud().levelCounter().show();
        game.session().hud().livesCounter().hide();
        game.session().hud().gameScore().data().setEnabled(true);
        game.session().hud().gameScore().show();
        game.session().hud().highScore().data().setEnabled(true);
        game.session().hud().highScore().show();
        game.session().hud().creditDisplay().show();

        game.session().store(SessionKey.HUD_VISIBLE, true);
        game.session().removeLevel();

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (timer().hasExpired()) {
            game.flow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
