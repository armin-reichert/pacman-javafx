/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;

public class Tengen_GameStartingState extends AbstractGameState {

    static final short TICK_SHOW_READY = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    public Tengen_GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnterState(GameContext game) {
        final var tengenGamePlay = (TengenMsPacMan_GamePlay) gamePlay;

        final GameLevel level = tengenGamePlay.buildNormalLevel(game, tengenGamePlay.startLevelNumber(session));
        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

        ScoreSystem.enableScore(hud.highScore(), true);
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameLevel level = session.level();
        final long tick = timer().tickCount();

        if (tick == TICK_SHOW_READY) {
            gamePlay.startLevel(game, level);
        }
        else if (tick == TICK_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_START_PLAYING) {
            session.setGameRunning(true);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
