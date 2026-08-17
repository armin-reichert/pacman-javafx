/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;

/**
 * Corresponds to the screen showing the people that have contributed to the game. Here, a second
 * screen with the contributors to the remake is shown too.
 */
public class ShowingHallOfFameState extends GameState {

    public ShowingHallOfFameState() {
        super(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
    }

    @Override
    public void onEnter(GameContext game) {
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterState(game, CommonGameStateID.GAME_INTRO);
        }
    }
}
