/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;

/**
 * Corresponds to the screen showing the people that have contributed to the game. Here, a seconds
 * screen with the contributors to the remake has been added.
 */
public class ShowingHallOfFameState extends GameState {

    public ShowingHallOfFameState() {
        super(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
    }

    @Override
    public void onEnter(GameContext context) {
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.flow().enterState(GameStateID.GAME_INTRO);
        }
    }
}
