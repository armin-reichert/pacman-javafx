/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

public class GameStartingState extends GameState {

    static final short TICK_SHOW_READY = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_PLAYING = 250;

    public GameStartingState() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext game) {
        final var model = (TengenMsPacMan_GameModel) game.model();
        final var gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();
        final GameSession session = game.session();

        gamePlay.buildNormalLevel(game, gamePlay.startLevelNumber(session), model.initialLifeCount());
        game.eventManager().publishGameEvent(new GameStartedEvent(game));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameSession session = gameContext.session();
        final GameLevel level = session.assertLevel();
        final long tick = timer().tickCount();

        if (tick == TICK_SHOW_READY) {
            gameContext.gamePlay().startLevel(gameContext);
            // Note: This event is very important because it triggers the creation of the actor animations!
            gameContext.eventManager().publishGameEvent(new LevelStartedEvent(level));
        }
        else if (tick == TICK_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_START_PLAYING) {
            session.setPlaying(true);
            gameContext.flow().enterState(gameContext, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
