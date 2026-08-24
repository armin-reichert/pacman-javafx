/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public class Common_GameOrLevelStartingState extends GameState {

    public Common_GameOrLevelStartingState() {
        super(CommonGameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        game.session().hud().showScore().showLevelCounter().showHUD();
    }

    @Override
    public void onUpdate(GameContext game) {
        game.variant().gameFlow().enterGameState(game, computeNextState(game));
    }

    private CommonGameStateID computeNextState(GameContext game) {
        if (game.session().isGameRunning()) {
            return CommonGameStateID.GAME_LEVEL_CONTINUE;
        }
        if (game.variant().gamePlay().canStart(game)) {
            return CommonGameStateID.GAME_STARTING;
        }
        return  CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
