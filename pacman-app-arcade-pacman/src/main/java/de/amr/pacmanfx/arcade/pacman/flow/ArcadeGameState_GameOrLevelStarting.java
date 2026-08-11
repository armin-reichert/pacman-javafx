/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;

public class ArcadeGameState_GameOrLevelStarting extends GameState {

    public ArcadeGameState_GameOrLevelStarting() {
        super(CommonGameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.session().hud().showScore().showLevelCounter().show();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        gameContext.flow().enterState(gameContext, computeNextState(gameContext));
    }

    private CommonGameStateID computeNextState(GameContext gameContext) {
        if (gameContext.session().isPlaying()) {
            return CommonGameStateID.GAME_LEVEL_CONTINUE;
        }
        if (!gameContext.coinMechanism().isEmpty()) {
            return CommonGameStateID.GAME_STARTING;
        }
        return  CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
