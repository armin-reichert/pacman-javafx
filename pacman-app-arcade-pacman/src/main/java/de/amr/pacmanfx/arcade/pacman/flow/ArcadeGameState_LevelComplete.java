/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;

public class ArcadeGameState_LevelComplete extends GameState {

    public ArcadeGameState_LevelComplete() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameSession session = gameContext.session();
        gameContext.gamePlay().onLevelCompleted(gameContext, session.assertLevel());
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController gameFlow = gameContext.flow();
        if (timer().hasExpired()) {
            gameFlow.enterState(gameContext, computeNextState(gameContext, gameFlow.cutScenesEnabled()));
        }
    }

    private CommonGameStateID computeNextState(GameContext game, boolean cutScenesEnabled) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        if (session.isAttractMode()) {
            // just in case: if demo level was completed, go back to intro scene
            return CommonGameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = game.model().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }
        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
