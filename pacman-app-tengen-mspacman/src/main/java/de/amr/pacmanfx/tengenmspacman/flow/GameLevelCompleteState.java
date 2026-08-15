/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

import static java.util.Objects.requireNonNull;

public class GameLevelCompleteState extends GameState {

    public GameLevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext game) {
        requireNonNull(game);

        game.variant().gamePlay().onLevelCompleted(game, game.session().assertLevel());
        waitForTimeout(); // Wait for UI to trigger timeout
    }

    @Override
    public void onUpdate(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameFlowController flow = game.variant().gameFlow();

        if (session.isAttractMode()) {
            flow.enterState(game, TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
            return;
        }

        if (timer().hasExpired()) {
            flow.enterState(game, computeNextState(game));
        }
    }

    private CommonGameStateID computeNextState(GameContext game) {
        final GameSession session = game.session();

        // Just in case: if demo level is complete, go back to intro scene
        if (session.isAttractMode()) {
            return CommonGameStateID.GAME_INTRO;
        }

        final GameLevel level = session.assertLevel();
        final boolean cutSceneFollows = game.variant().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && session.cutScenesEnabled()) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }
        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
