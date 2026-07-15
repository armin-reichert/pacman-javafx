/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlow;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameOrLevelStartingState extends GameState {

    public ArcadeGameOrLevelStartingState() {
        super(GameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        model.hudState().showScore().showLevelCounter().show();
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameFlow flow = context.flow();
        final GameModel model = context.model();

        if (model.isPlaying()) {
            flow.enterState(GameStateID.GAME_LEVEL_CONTINUE);
        }
        else if (!context.coinMechanism().isEmpty()) {
            flow.enterState(GameStateID.GAME_STARTING);
        }
        else {
            flow.enterState(GameStateID.DEMO_LEVEL_PLAYING);
        }
    }

}
