/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public final class Common_PreparationState extends AbstractGameState {

    public Common_PreparationState() {
        super(CommonGameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnterState(GameContext game) {
        // We might enter this state from the demo level playing state
        session.optLevel().ifPresent(level -> {
            level.entities().theMessageView().hide();
            session.setLevel(null);
        });

        session.setNumLives(game.variant().initialLifeCount());

        hud.creditDisplay().show();
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().hide();
        hud.show();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
