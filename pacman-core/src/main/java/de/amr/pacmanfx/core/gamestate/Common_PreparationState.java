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
        game.session().optLevel().ifPresent(level -> {
            level.entitySet().theOne(MessageView.class).hide();
            game.session().removeLevel();
        });

        game.session().setNumLives(game.runtime().gameRules().initialLifeCount());
        game.session().store(SessionKey.HUD_VISIBLE, true);

        game.session().hud().creditDisplay().show();
        game.session().hud().gameScore().show();
        game.session().hud().levelCounter().show();
        game.session().hud().livesCounter().hide();
    }

    @Override
    public void onUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
