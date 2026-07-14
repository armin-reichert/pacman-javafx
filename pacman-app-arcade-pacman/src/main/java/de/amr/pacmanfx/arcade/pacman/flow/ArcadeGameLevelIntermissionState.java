/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlow;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.GameModel;

public class ArcadeGameLevelIntermissionState extends GameState {

    public ArcadeGameLevelIntermissionState() {
        super(GameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        model.hudState().hideCredit().hideScore().showLevelCounter().hideLivesCounter().show();
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameFlow flow = context.flow();
        final GameModel model = context.model();

        if (timer().hasExpired()) {
            flow.enterState(model.isPlaying() ? GameStateID.GAME_LEVEL_TRANSITION : GameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel model = context.model();
        model.hudState().hideCredit().showScore().showLevelCounter().showLivesCounter().show();
    }
}
