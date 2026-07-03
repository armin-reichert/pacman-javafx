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
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;

public class GameLevelIntermissionState extends GameState {

    public GameLevelIntermissionState() {
        super(GameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final var hudState = gameModel.hudState();
        final boolean isLastCutScene = level.cutSceneNumber() == gameContext.rules().lastCutSceneNumber();

        if (gameModel.mapCategory() == MapCategory.ARCADE || isLastCutScene) {
            hudState.hideIt();
        }
        else {
            hudState.gameOptionsOff().scoreOff().levelCounterOn().livesCounterOff().showIt();
        }
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
        final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameContext.model();
        final TengenMsPacMan_HUDState hudState = gameModel.hudState();

        if (gameModel.mapCategory() == MapCategory.ARCADE) {
            hudState.hideIt();
        }
        else {
            hudState.gameOptionsOn().scoreOn().levelCounterOn().livesCounterOff().showIt();
        }
    }
}
