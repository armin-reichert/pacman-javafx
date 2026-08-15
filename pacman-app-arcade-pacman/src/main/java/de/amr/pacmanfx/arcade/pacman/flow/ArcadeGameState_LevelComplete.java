/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

public class ArcadeGameState_LevelComplete extends GameState {

    public ArcadeGameState_LevelComplete() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        game.variantConfig().gamePlay().onLevelCompleted(game, session.assertLevel());
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameFlowController flow = game.session().gameFlow();
        if (timer().hasExpired()) {
            flow.enterState(game, computeNextState(game, game.session().cutScenesEnabled()));
        }
    }

    private CommonGameStateID computeNextState(GameContext game, boolean cutScenesEnabled) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        if (session.isAttractMode()) {
            // just in case: if demo level was completed, go back to intro scene
            return CommonGameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = game.variantConfig().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }
        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
