/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;

public class ArcadeGameLevelCompleteState extends GameState {

    public ArcadeGameLevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.gamePlay().onLevelCompleted(gameContext);
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController gameFlow = gameContext.flow();
        if (timer().hasExpired()) {
            gameFlow.enterState(gameContext, computeNextState(gameContext, gameFlow.cutScenesEnabled()));
        }
    }

    private CommonGameStateID computeNextState(GameContext gameContext, boolean cutScenesEnabled) {
        final GameLevel level = gameContext.assertLevel();
        if (level.isDemoLevel()) {
            // just in case: if demo level was completed, go back to intro scene
            return CommonGameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = gameContext.model().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }
        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
