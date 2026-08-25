/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.Named;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;

public class Common_LevelCompleteState extends GameState {

    public Common_LevelCompleteState() {
        super(CommonGameStateID.GAME_LEVEL_COMPLETE);
    }

    @Override
    public void onEnter(GameContext game) {
        game.variant().gamePlay().finishLevel(game, game.session().level());
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        game.variant().systems().entityUpdater().updateHUD(game);
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterGameState(game, computeNextStateID(game));
        }
    }

    protected Named computeNextStateID(GameContext game) {
        final GameSession session = game.session();

        // Just in case: if demo level was completed, go back to intro scene
        if (session.isAttractMode()) {
            return CommonGameStateID.GAME_INTRO;
        }

        final GameLevel level = session.level();
        final boolean cutSceneFollows = game.variant().rules().cutSceneAfterLevel(level.number()).isPresent();
        if (cutSceneFollows && session.cutScenesEnabled()) {
            return CommonGameStateID.GAME_LEVEL_INTERMISSION;
        }

        return CommonGameStateID.GAME_LEVEL_TRANSITION;
    }
}
