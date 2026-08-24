/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;

public final class Common_PreparationState extends GameState {

    public Common_PreparationState() {
        super(CommonGameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        session.setNumLives(game.variant().initialLifeCount());
        session.hud().showCredit().showScore().showLevelCounter().hideLivesCounter().showHUD();
    }

    @Override
    public void onUpdate(GameContext game) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
