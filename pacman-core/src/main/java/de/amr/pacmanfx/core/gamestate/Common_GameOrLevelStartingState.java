/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;

public class Common_GameOrLevelStartingState extends AbstractGameState {

    public Common_GameOrLevelStartingState() {
        super(CommonGameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        game.session().hud().gameScore().show();
        game.session().hud().highScore().show();
        game.session().store(SessionKey.HUD_VISIBLE, true);
    }

    @Override
    public void onUpdate(GameContext game, long globalTick, long stateTick) {
        if (game.session().optLevel().isPresent()) {
            // When state is entered, level is not yet created and no level/lives counter should be shown
            game.session().hud().levelCounter().show();
            game.session().hud().livesCounter().show();
        }
        game.flow().enterGameState(game, computeNextState(game));
    }

    private CommonGameStateID computeNextState(GameContext game) {
        if (game.session().booleanValue(SessionKey.GAME_RUNNING)) {
            return CommonGameStateID.GAME_LEVEL_CONTINUE;
        }
        if (game.play().canStart(game)) {
            return CommonGameStateID.GAME_STARTING;
        }
        return  CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
