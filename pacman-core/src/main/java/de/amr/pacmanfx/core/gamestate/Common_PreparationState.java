/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;

public final class Common_PreparationState extends AbstractGameState {

    public Common_PreparationState() {
        super(CommonGameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnterState(GameContext game) {
        // We might enter this state from the demo level playing state
        session.optLevel().ifPresent(level -> {
            level.entitySet().theOne(MessageView.class).hide();
            session.setLevel(null);
        });

        session.setValue(SessionKey.NUM_LIVES, game.runtime().gameRules().initialLifeCount());
        session.setValue(SessionKey.HUD_VISIBLE, true);

        hud.creditDisplay().show();
        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().hide();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
