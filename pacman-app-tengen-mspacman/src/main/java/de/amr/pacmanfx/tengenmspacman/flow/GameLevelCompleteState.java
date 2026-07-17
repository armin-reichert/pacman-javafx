/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class GameLevelCompleteState extends GameState {

    public GameLevelCompleteState() {
        super(GameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.gamePlay().onLevelCompleted(gameContext.assertLevel());
        waitForTimeout(); // Wait for UI to trigger timeout
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController flow = gameContext.flow();

        if (gameContext.assertLevel().isDemoLevel()) {
            flow.enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
            return;
        }

        if (timer().hasExpired()) {
            flow.enterState(computeNextState(gameContext, flow.cutScenesEnabled()));
        }
    }

    private GameStateID computeNextState(GameContext gameContext, boolean cutScenesEnabled) {
        final GameLevel level = gameContext.assertLevel();
        if (level.isDemoLevel()) { // Just in case: if demo level is completed, go back to intro scene
            return GameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = gameContext.model().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return GameStateID.GAME_LEVEL_INTERMISSION;
        }
        return GameStateID.GAME_LEVEL_TRANSITION;
    }
}
