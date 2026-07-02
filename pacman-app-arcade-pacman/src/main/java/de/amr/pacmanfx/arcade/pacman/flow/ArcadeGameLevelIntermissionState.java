/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;

public class ArcadeGameLevelIntermissionState extends GameState {

    public ArcadeGameLevelIntermissionState() {
        super(GameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.hudState().creditOff().scoreOff().levelCounterOn().livesCounterOff().showIt();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();

        if (timer().hasExpired()) {
            flow.enterState(gameModel.isPlaying() ? GameStateID.GAME_LEVEL_TRANSITION : GameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.hudState().creditOff().scoreOn().levelCounterOn().livesCounterOn().showIt();
    }

}
