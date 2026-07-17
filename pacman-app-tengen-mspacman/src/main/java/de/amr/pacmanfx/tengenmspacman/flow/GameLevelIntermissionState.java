/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;

import java.util.OptionalInt;

public class GameLevelIntermissionState extends GameState {

    public GameLevelIntermissionState() {
        super(GameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) gameContext.model();
        final GameLevel level = model.assertLevel();
        final OptionalInt cutSceneNumber = model.rules().cutSceneNumberAfterLevel(level.number());
        final boolean isLastCutScene = cutSceneNumber.isPresent()
            && cutSceneNumber.getAsInt() == model.rules().lastCutSceneNumber();
        final var hudState = model.hudState();

        if (isLastCutScene) {
            hudState.hide();
        } else {
            hudState.hideGameOptions().hideScore().showLevelCounter().hideLivesCounter().show();
        }
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController flow = gameContext.flow();
        final GameModel model = gameContext.model();

        if (timer().hasExpired()) {
            flow.enterState(model.isPlaying() ? GameStateID.GAME_LEVEL_TRANSITION : GameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) gameContext.model();
        final TengenMsPacMan_HUDState hudState = model.hudState();

        if (model.mapCategory() == MapCategory.ARCADE) {
            hudState.hide();
        }
        else {
            hudState.showGameOptions().showScore().showLevelCounter().hideLivesCounter().show();
        }
    }
}
