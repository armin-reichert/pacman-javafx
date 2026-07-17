/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameOrLevelStartingState extends GameState {

    public ArcadeGameOrLevelStartingState() {
        super(GameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.model().hudState().showScore().showLevelCounter().show();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        gameContext.flow().enterState(computeNextState(gameContext));
    }

    private GameStateID computeNextState(GameContext gameContext) {
        if (gameContext.model().isPlaying()) {
            return GameStateID.GAME_LEVEL_CONTINUE;
        }
        if (!gameContext.coinMechanism().isEmpty()) {
            return GameStateID.GAME_STARTING;
        }
        return  GameStateID.DEMO_LEVEL_PLAYING;
    }
}
