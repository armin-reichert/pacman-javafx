/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.model.HUDState;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import org.tinylog.Logger;

public class GameOrLevelStartingState extends GameState {

    public GameOrLevelStartingState() {
        super(CommonGameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();
        final GameSession session = game.session();
        final HUDState hudState = session.hud();
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) game.model();

        hudState.hideCredit().showScore().showLevelCounter().showLivesCounter().show();
        // The rules vary between map categories so update the rules here:
        model.rules().setMapCategory(gamePlay.mapCategory(session));
        Logger.info("Using game rules for map category {}", model.rules().mapCategory());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (!(gameContext.model() instanceof TengenMsPacMan_GameModel model)) {
            throw new IllegalStateException("Illegal game model: " + gameContext.model());
        }
        gameContext.flow().enterState(gameContext, computeNextState(model, gameContext.session()));
    }

    private CommonGameStateID computeNextState(TengenMsPacMan_GameModel model, GameSession session) {
        if (session.isPlaying()) {
            return CommonGameStateID.GAME_LEVEL_CONTINUE;
        }
        if (model.canStartNewGame()) {
            return CommonGameStateID.GAME_STARTING;
        }
        return CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
