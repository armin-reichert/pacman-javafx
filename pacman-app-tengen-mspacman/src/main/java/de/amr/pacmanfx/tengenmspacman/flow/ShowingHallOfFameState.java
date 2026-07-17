/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

/**
 * Corresponds to the screen showing the people that have contributed to the game. Here, a second
 * screen with the contributors to the remake is shown too.
 */
public class ShowingHallOfFameState extends GameState {

    public ShowingHallOfFameState() {
        super(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            gameContext.flow().enterState(GameStateID.GAME_INTRO);
        }
    }
}
