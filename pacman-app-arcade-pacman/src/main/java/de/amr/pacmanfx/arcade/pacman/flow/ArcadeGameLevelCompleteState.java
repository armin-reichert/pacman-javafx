/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;

public class ArcadeGameLevelCompleteState extends GameState {

    public ArcadeGameLevelCompleteState() {
        super(GameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.onLevelCompleted(gameContext.assertLevel());
        waitForTimeout(); // UI triggers timeout
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameLevel level = gameContext.assertLevel();

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                // just in case: if demo level was completed, go back to intro scene
                flow.enterState(GameStateID.GAME_INTRO);
            }
            else if (flow.cutScenesEnabled() && level.cutSceneNumber() != 0) {
                flow.enterState(GameStateID.GAME_LEVEL_INTERMISSION);
            }
            else {
                flow.enterState(GameStateID.GAME_LEVEL_TRANSITION);
            }
        }
    }
}
