/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
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
        level = gamePlay.buildNormalLevel(game, 1);

        hud.creditDisplay().hide();
        hud.livesCounter().show();
        hud.levelCounter().show();
        hud.gameScore().show();
        hud.highScore().show();
        hud.show();

        hud.highScore().data().setEnabled(true);

        lockPacAndGhosts(level.entities(), true);

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (stateTick == TICK_START_LEVEL) {
            gamePlay.startLevel(game, level);
        }
        else if (stateTick == TICK_SHOW_GUYS) {
            showPacAndGhosts(level.entities());
        }
        else if (stateTick == TICK_START_PLAYING) {
            final Pac pac = level.entities().pac();
            lockPacAndGhosts(level.entities(), false);
            pac.state().setEnumValue(PacState.ACTIVE);

            game.coinMechanism().consumeCoin();
            session.setGameRunning(true);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
