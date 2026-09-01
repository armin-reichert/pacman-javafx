/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public class Common_GameOrLevelStartingState extends AbstractGameState {

    public Common_GameOrLevelStartingState() {
        super(CommonGameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.show();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        flow.enterGameState(game, computeNextState(game));
    }

    private CommonGameStateID computeNextState(GameContext game) {
        if (session.isGameRunning()) {
            return CommonGameStateID.GAME_LEVEL_CONTINUE;
        }
        if (gamePlay.canStart(game)) {
            return CommonGameStateID.GAME_STARTING;
        }
        return  CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
