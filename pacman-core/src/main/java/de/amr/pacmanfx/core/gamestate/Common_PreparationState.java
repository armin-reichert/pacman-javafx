/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;

public final class Common_PreparationState extends AbstractGameState {

    public Common_PreparationState() {
        super(CommonGameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnterState(GameContext game) {
        session.setNumLives(game.variant().initialLifeCount());

        hud.showCredit();
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().hide();
        hud.show();
    }

    @Override
    public void onUpdate(GameContext game) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
