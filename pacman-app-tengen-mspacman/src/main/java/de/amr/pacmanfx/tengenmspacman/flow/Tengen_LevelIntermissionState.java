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

public class Tengen_LevelIntermissionState extends GameState {

    public Tengen_LevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final OptionalInt cutSceneNumber = game.variant().rules().cutSceneAfterLevel(level.number());
        final boolean isLastCutScene = cutSceneNumber.isPresent()
            && cutSceneNumber.getAsInt() == game.variant().rules().lastCutSceneNumber();

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
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterGameState(game, session.isPlaying()
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.variant().gamePlay();
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
