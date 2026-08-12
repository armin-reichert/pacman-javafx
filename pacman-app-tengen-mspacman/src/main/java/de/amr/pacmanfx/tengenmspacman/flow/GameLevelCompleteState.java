/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;

public class GameLevelCompleteState extends GameState {

    public GameLevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext game) {
        game.gamePlay().onLevelCompleted(game, game.session().assertLevel());
        waitForTimeout(); // Wait for UI to trigger timeout
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameFlowController flow = game.session().gameFlow();

        if (game.session().isAttractMode()) {
            flow.enterState(game, TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
            return;
        }

        if (timer().hasExpired()) {
            flow.enterState(game, computeNextState(game, flow.cutScenesEnabled()));
        }
    }

    private CommonGameStateID computeNextState(GameContext game, boolean cutScenesEnabled) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        if (session.isAttractMode()) { // Just in case: if demo level is completed, go back to intro scene
            return CommonGameStateID.GAME_INTRO;
        }
        final boolean cutSceneFollows = game.model().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && cutScenesEnabled) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }
        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
