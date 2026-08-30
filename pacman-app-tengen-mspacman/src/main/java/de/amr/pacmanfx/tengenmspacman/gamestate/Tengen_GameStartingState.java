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
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;

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
        level = gamePlay.buildNormalLevel(game, TengenMsPacMan_GamePlay.startLevelNumber(session));

        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

        hud.hideCredit();
        hud.livesCounter().show();
        hud.levelCounter().show();
        hud.gameScore().show();
        hud.highScore().show();
        hud.show();

        systems.scoreSystem().enableScore(hud.highScore(), true);

        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        if (tick < TICK_START_PLAYING) {
            freezeActors(level.entities());
        }

        if (tick == TICK_START_LEVEL) {
            gamePlay.startLevel(game, level);
        }
        else if (tick == TICK_SHOW_GUYS) {
            showActors(level.entities());
        }
        else if (tick == TICK_START_PLAYING) {
            unfreezeActors(level.entities());
            game.coinMechanism().consumeCoin();
            session.setGameRunning(true);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
