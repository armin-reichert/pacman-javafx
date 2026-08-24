/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.Common_LevelCompleteState;
import de.amr.pacmanfx.core.level.GameLevel;

/**
 * Tengen Ms. Pac-Man has a "hall of fame" screen which is shown after the demo level completes.
 */
public class Tengen_LevelCompleteState extends Common_LevelCompleteState {

    public Tengen_LevelCompleteState() {}

    @Override
    protected Named computeNextStateID(GameContext game) {
        final GameSession session = game.session();

        if (session.isAttractMode()) {
            return TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME;
        }

        final GameLevel level = session.level();
        final boolean cutScene = game.variant().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutScene && session.cutScenesEnabled()) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }

        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
