/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;

public class ArcadeGameOrLevelStartingState extends GameState {

    public ArcadeGameOrLevelStartingState() {
        super(GameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.hudState().scoreOn().levelCounterOn().showIt();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();

        if (gameModel.isPlaying()) {
            flow.enterState(GameStateID.GAME_LEVEL_CONTINUE);
        }
        else if (gameModel.canStartNewGame(gameContext)) {
            flow.enterState(GameStateID.GAME_STARTING);
        }
        else {
            flow.enterState(GameStateID.DEMO_LEVEL_PLAYING);
        }
    }

}
