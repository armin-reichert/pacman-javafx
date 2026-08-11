/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.session.GameSession;

public class ArcadeGameState_LevelIntermission extends GameState {

    public ArcadeGameState_LevelIntermission() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final HUDState hudState = gameContext.session().hud();
        hudState.hideCredit().hideScore().showLevelCounter().hideLivesCounter().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameSession session = gameContext.session();
        final GameFlowController gameFlow = gameContext.flow();
        if (timer().hasExpired()) {
            gameFlow.enterState(gameContext, session.isPlaying() ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        gameContext.session().hud()
            .hideCredit().showScore().showLevelCounter().showLivesCounter().show();
    }
}
