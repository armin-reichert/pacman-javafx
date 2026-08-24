/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

import static java.util.Objects.requireNonNull;

public class Tengen_LevelCompleteState extends GameState {

    public Tengen_LevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext game) {
        requireNonNull(game);

        game.variant().gamePlay().finishLevel(game, game.session().level());
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameFlowController flow = game.variant().gameFlow();

        game.variant().systems().entityUpdater().updateSessionHUDEntities(game);

        if (session.isAttractMode()) {
            flow.enterGameState(game, TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
            return;
        }

        if (timer().hasExpired()) {
            flow.enterGameState(game, computeNextState(game));
        }
    }

    private CommonGameStateID computeNextState(GameContext game) {
        final GameSession session = game.session();

        // Just in case: if demo level is complete, go back to intro scene
        if (session.isAttractMode()) {
            return CommonGameStateID.GAME_INTRO;
        }

        final GameLevel level = session.level();
        final boolean cutSceneFollows = game.variant().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && session.cutScenesEnabled()) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }
        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
