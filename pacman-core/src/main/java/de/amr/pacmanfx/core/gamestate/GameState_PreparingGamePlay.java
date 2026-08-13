/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;

public final class GameState_PreparingGamePlay extends GameState {

    public GameState_PreparingGamePlay() {
        super(CommonGameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        session.hud()
            .showCredit().showScore().showLevelCounter().hideLivesCounter().show();
    }

    @Override
    public void onUpdate(GameContext game) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
