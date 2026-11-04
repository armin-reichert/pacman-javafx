/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;

/**
 * Corresponds to the screen showing the people that have contributed to the game. Here, a second
 * screen with the contributors to the remake is shown too.
 */
public class Tengen_ShowingHallOfFameState extends AbstractGameState {

    public Tengen_ShowingHallOfFameState() {
        super(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
    }

    @Override
    public void doEnterState(GameContext game) {
        stateTimer().resetToIndefiniteDuration();
    }

    @Override
    public void doUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        if (stateTimer().hasExpired()) {
            game.flow().enterGameState(game, CommonGameStateID.GAME_INTRO);
        }
    }
}
