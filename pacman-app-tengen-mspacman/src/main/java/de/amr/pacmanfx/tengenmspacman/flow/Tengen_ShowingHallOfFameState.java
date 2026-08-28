/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

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
    public void onEnterState(GameContext game) {
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            flow.enterGameState(game, CommonGameStateID.GAME_INTRO);
        }
    }
}
