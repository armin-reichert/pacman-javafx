/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.GameSession;

public class Arcade_LevelIntermissionState extends GameState {

    public Arcade_LevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext game) {
        final HUD hud = game.session().hud();
        hud.hideCredit();
        hud.gameScore().hide();
        hud.levelCounter().show();
        hud.livesCounter().hide();
        hud.show();

        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameFlowController flow = game.variant().gameFlow();
        if (timer().hasExpired()) {
            flow.enterGameState(game, session.isGameRunning()
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final HUD hud = game.session().hud();
        hud.hideCredit();
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().show();
        hud.show();
    }
}
