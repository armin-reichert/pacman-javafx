/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameLevelCompleteState extends GameState {

    public ArcadeGameLevelCompleteState() {
        super(GameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        context.gamePlay().onLevelCompleted(model.assertLevel());
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
        if (gameContext.level().isDemoLevel()) {
            // just in case: if demo level was completed, go back to intro scene
            return GameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = !gameContext.level().isDemoLevel()
            && gameContext.model().rules().cutSceneNumberAfterLevel(gameContext.level().number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return GameStateID.GAME_LEVEL_INTERMISSION;
        }
        return GameStateID.GAME_LEVEL_TRANSITION;
    }
}
