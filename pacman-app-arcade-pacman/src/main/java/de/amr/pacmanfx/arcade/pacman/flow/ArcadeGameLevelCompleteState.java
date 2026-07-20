/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameLevelCompleteState extends GameState {

    public ArcadeGameLevelCompleteState() {
        super(GameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.gamePlay().onLevelCompleted(gameContext.assertLevel());
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController gameFlow = gameContext.flow();
        if (timer().hasExpired()) {
            gameFlow.enterState(gameContext, computeNextState(gameContext, gameFlow.cutScenesEnabled()));
        }
    }

    private GameStateID computeNextState(GameContext gameContext, boolean cutScenesEnabled) {
        final GameLevel level = gameContext.assertLevel();
        if (level.isDemoLevel()) {
            // just in case: if demo level was completed, go back to intro scene
            return GameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = gameContext.model().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return GameStateID.GAME_LEVEL_INTERMISSION;
        }
        return GameStateID.GAME_LEVEL_TRANSITION;
    }
}
