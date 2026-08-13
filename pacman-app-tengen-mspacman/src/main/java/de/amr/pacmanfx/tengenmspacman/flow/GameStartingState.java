/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;

public class GameStartingState extends GameState {

    static final short TICK_SHOW_READY = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    public GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        final var gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();
        final GameSession session = game.session();

        gamePlay.buildNormalLevel(game, gamePlay.startLevelNumber(session), game.initialLifeCount());
        ScoreSystem.enableScore(session.highScore(), true);
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final long tick = timer().tickCount();

        if (tick == TICK_SHOW_READY) {
            game.gamePlay().startLevel(game);
            // Note: This event is very important because it triggers the creation of the actor animations!
            game.eventManager().publishGameEvent(new LevelStartedEvent(level));
        }
        else if (tick == TICK_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_START_PLAYING) {
            session.setPlaying(true);
            session.gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
