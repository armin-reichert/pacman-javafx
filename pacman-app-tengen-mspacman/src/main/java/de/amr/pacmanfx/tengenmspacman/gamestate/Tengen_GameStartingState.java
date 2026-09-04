/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.startLevelNumber;

public class Tengen_GameStartingState extends AbstractGameState {

    static final short TICK_START_LEVEL = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    private GameLevel level;

    public Tengen_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = gamePlay.buildNormalLevel(game, startLevelNumber(session));

        hud.creditDisplay().hide();
        hud.livesCounter().show();
        hud.levelCounter().show();
        hud.gameScore().show();
        hud.highScore().show();
        hud.show();

        hud.highScore().data().setEnabled(true);

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (stateTick < TICK_START_PLAYING) {
            lockPacAndGhosts(level.entities(), true);
        }

        if (stateTick == TICK_START_LEVEL) {
            gamePlay.startLevel(game, level);
        }
        else if (stateTick == TICK_SHOW_GUYS) {
            showPacAndGhosts(level.entities());
        }
        else if (stateTick == TICK_START_PLAYING) {
            lockPacAndGhosts(level.entities(), false);
            game.coinMechanism().consumeCoin();
            session.setGameRunning(true);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
