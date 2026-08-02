/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.GameModel;

public class ArcadeGameLevelIntermissionState extends GameState {

    public ArcadeGameLevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.hudState().hideCredit().hideScore().showLevelCounter().hideLivesCounter().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController gameFlow = gameContext.flow();
        final GameModel model = gameContext.model();
        if (timer().hasExpired()) {
            gameFlow.enterState(gameContext, model.isPlaying() ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        gameContext.hudState().hideCredit().showScore().showLevelCounter().showLivesCounter().show();
    }
}
