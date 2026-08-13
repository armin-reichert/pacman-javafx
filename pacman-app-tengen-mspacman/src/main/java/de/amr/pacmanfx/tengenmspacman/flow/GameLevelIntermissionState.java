/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

import java.util.OptionalInt;

public class GameLevelIntermissionState extends GameState {

    public GameLevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final OptionalInt cutSceneNumber = game.variantConfig().rules().cutSceneAfterLevel(level.number());
        final boolean isLastCutScene = cutSceneNumber.isPresent()
            && cutSceneNumber.getAsInt() == game.variantConfig().rules().lastCutSceneNumber();

        if (isLastCutScene) {
            session.hud().hide();
        } else {
            session.hud()
                .hideGameOptions()
                .hideScore()
                .showLevelCounter()
                .hideLivesCounter()
                .show();
        }
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        if (timer().hasExpired()) {
            session.gameFlow().enterState(game, session.isPlaying()
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.variantConfig().gamePlay();
        final GameSession session = game.session();
        final HUDState hudState = session.hud();
        if (gamePlay.mapCategory(session) == MapCategory.ARCADE) {
            hudState.hide();
        } else {
            hudState
                .showGameOptions()
                .showScore()
                .showLevelCounter()
                .hideLivesCounter()
                .show();
        }
    }
}
