/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;

public class GameLevelCompleteState extends GameState {

    public GameLevelCompleteState() {
        super(GameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.onLevelCompleted(gameModel.assertLevel());
        waitForTimeout(); // Waits for UI to trigger timeout
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.assertLevel();
        final boolean cutSceneFollows = !level.isDemoLevel()
            && gameContext.model().rules().cutSceneNumberAfterLevel(level.number()).isPresent();

        if (level.isDemoLevel()) {
            flow.enterState(TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
            return;
        }

        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                // Just in case: if demo level is completed, go back to intro scene
                flow.enterState(GameStateID.GAME_INTRO);
            }
            else if (cutSceneFollows && flow.cutScenesEnabled()) {
                flow.enterState(GameStateID.GAME_LEVEL_INTERMISSION);
            }
            else {
                flow.enterState(GameStateID.GAME_LEVEL_TRANSITION);
            }
        }
    }

}
