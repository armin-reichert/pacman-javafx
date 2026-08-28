/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;

public class Arcade_LevelIntermissionState extends AbstractGameState {

    public Arcade_LevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnterState(GameContext game) {
        hud.hideCredit();
        hud.gameScore().hide();
        hud.levelCounter().show();
        hud.livesCounter().hide();
        hud.show();

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            flow.enterGameState(game, session.isGameRunning()
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        hud.hideCredit();
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().show();
        hud.show();
    }
}
