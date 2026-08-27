/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.Common_LevelCompleteState;

/**
 * Tengen Ms. Pac-Man has a "hall of fame" screen which is shown after the demo level completes.
 */
public class Tengen_LevelCompleteState extends Common_LevelCompleteState {

    public Tengen_LevelCompleteState() {}

    @Override
    protected Named computeNextStateID() {
        if (session.isAttractMode()) {
            return TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME;
        }

        final boolean cutScene = rules.cutSceneAfterLevel(level.number()).isPresent();
        if (cutScene && session.cutScenesEnabled()) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }

        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
