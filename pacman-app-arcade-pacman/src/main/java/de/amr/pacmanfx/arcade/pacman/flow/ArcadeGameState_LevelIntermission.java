/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.session.GameSession;

public class ArcadeGameState_LevelIntermission extends GameState {

    public ArcadeGameState_LevelIntermission() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext game) {
        final HUDState hudState = game.session().hud();
        hudState.hideCredit().hideScore().showLevelCounter().hideLivesCounter().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameFlowController flow = game.session().gameFlow();
        if (timer().hasExpired()) {
            flow.enterState(game, session.isPlaying()
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        game.session().hud()
            .hideCredit().showScore().showLevelCounter().showLivesCounter().show();
    }
}
