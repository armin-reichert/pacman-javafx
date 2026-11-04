/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;

public class Arcade_LevelIntermissionState extends AbstractGameState {

    public Arcade_LevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void doEnterState(GameContext game) {
        game.session().hud().creditDisplay().hide();
        game.session().hud().gameScore().hide();
        game.session().hud().highScore().hide();
        game.session().hud().livesCounter().hide();
        game.session().hud().levelCounter().show();
        game.session().store(SessionKey.HUD_VISIBLE, true);

        stateTimer().resetToIndefiniteDuration();
    }

    @Override
    public void doUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        if (stateTimer().hasExpired()) {
            game.flow().enterGameState(game, game.session().booleanValue(SessionKey.GAME_RUNNING)
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        game.session().hud().gameScore().show();
        game.session().hud().highScore().show();
        game.session().hud().livesCounter().show();
    }
}
