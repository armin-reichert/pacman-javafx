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
        hud.levelCounter().show();
        hud.livesCounter().hide();
        hud.gameScore().data().setEnabled(true);
        hud.gameScore().show();
        hud.highScore().data().setEnabled(true);
        hud.highScore().show();
        hud.creditDisplay().show();

        session.setValue(SessionKey.HUD_VISIBLE, true);
        session.removeValue(SessionKey.GAME_LEVEL);

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (timer().hasExpired()) {
            flow.enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
