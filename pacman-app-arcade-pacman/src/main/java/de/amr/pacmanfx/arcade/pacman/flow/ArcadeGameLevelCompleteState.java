/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameLevelCompleteState extends GameState {

    public ArcadeGameLevelCompleteState() {
        super(GameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        context.gamePlay().onLevelCompleted(model.assertLevel());
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameFlowController flow = context.flow();
        final GameLevel level = context.model().assertLevel();
        final boolean cutSceneFollows = !level.isDemoLevel()
            && context.model().rules().cutSceneNumberAfterLevel(level.number()).isPresent();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                // just in case: if demo level was completed, go back to intro scene
                flow.enterState(context, GameStateID.GAME_INTRO);
            }
            else if (cutSceneFollows && flow.cutScenesEnabled()) {
                flow.enterState(context, GameStateID.GAME_LEVEL_INTERMISSION);
            }
            else {
                flow.enterState(context, GameStateID.GAME_LEVEL_TRANSITION);
            }
        }
    }
}
