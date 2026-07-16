/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameLevelIntermissionState extends GameState {

    public ArcadeGameLevelIntermissionState() {
        super(GameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        model.hudState().hideCredit().hideScore().showLevelCounter().hideLivesCounter().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController gameFlow = gameContext.flow();
        final GameModel model = gameContext.model();

        if (timer().hasExpired()) {
            gameFlow.enterState(gameContext, model.isPlaying() ? GameStateID.GAME_LEVEL_TRANSITION : GameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        final GameModel model = gameContext.model();
        model.hudState().hideCredit().showScore().showLevelCounter().showLivesCounter().show();
    }
}
