/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.gameplay.GamePlay;

public class Common_GameOrLevelStartingState extends GameState {

    private GameFlowController gameFlow;
    private GamePlay gamePlay;
    private GameSession session;

    public Common_GameOrLevelStartingState() {
        super(CommonGameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        gameFlow = game.variant().gameFlow();
        gamePlay = game.variant().gamePlay();
        session = game.session();

        final HUD hud = session.hud();
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.show();
    }

    @Override
    public void onUpdate(GameContext game) {
        gameFlow.enterGameState(game, computeNextState(game));
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
