/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;

public class Arcade_GameStartingState extends AbstractGameState {

    static final int TICK_START_LEVEL = 2;
    static final int TICK_SHOW_GUYS = 60;
    static final int TICK_START_PLAYING = 240;

    private GameLevel level;

    public Arcade_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = game.play().buildNormalLevel(game, 1);

        game.session().hud().creditDisplay().hide();
        game.session().hud().livesCounter().show();
        game.session().hud().levelCounter().show();
        game.session().hud().gameScore().show();
        game.session().hud().highScore().show();
        game.session().store(SessionKey.HUD_VISIBLE, true);

        game.session().hud().highScore().data().setEnabled(true);

        lockPacAndGhosts(game, level.entitySet(), true);

        game.runtime().eventManager().publishEvent(new LevelCreatedEvent(game, level));
        game.runtime().eventManager().publishEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (stateTick == TICK_START_LEVEL) {
            game.play().startLevel(game, level);
        }
        else if (stateTick == TICK_SHOW_GUYS) {
            showPacAndGhosts(level.entitySet());
        }
        else if (stateTick == TICK_START_PLAYING) {
            final Pac pac = level.entitySet().pac();
            lockPacAndGhosts(game, level.entitySet(), false);
            pac.state().setEnumValue(PacState.ACTIVE);

            game.coinMechanism().consumeCoin();
            game.session().store(SessionKey.GAME_RUNNING, true);
            game.flow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
