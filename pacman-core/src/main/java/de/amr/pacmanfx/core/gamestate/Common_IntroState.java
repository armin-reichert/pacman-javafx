/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;

public final class Common_IntroState extends GameState {

    public Common_IntroState() {
        super(CommonGameStateID.GAME_INTRO);
    }

    @Override
    public void onEnter(GameContext game) {
        game.session().hud().showLevelCounter().hideLivesCounter().showCredit().showScore().showHUD();
        game.session().setLevel(null);
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
        }
    }
}
