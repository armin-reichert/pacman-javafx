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
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;
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
        final TengenMsPacMan_GameRules rules = (TengenMsPacMan_GameRules) game.rules();

        hudState.hideCredit().showScore().showLevelCounter().showLivesCounter().show();

        // TODO: Probably current map category should be stored in session and passed to rules
        rules.setMapCategory(gamePlay.mapCategory(session));
        Logger.info("Using game rules for map category {}", rules.mapCategory());
    }

    @Override
    public void onUpdate(GameContext game) {
        game.session().gameFlow().enterState(game, computeNextState(game));
    }

    private CommonGameStateID computeNextState(GameContext game) {
        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();
        if (game.session().isPlaying()) {
            return CommonGameStateID.GAME_LEVEL_CONTINUE;
        }
        if (gamePlay.canStartNewGame(game.session())) {
            return CommonGameStateID.GAME_STARTING;
        }
        return CommonGameStateID.DEMO_LEVEL_PLAYING;
    }
}
