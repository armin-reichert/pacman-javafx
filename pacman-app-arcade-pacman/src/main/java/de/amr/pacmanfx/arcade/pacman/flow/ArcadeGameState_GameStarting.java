/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.event.gameplay.GameStartedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GameModel;

public class ArcadeGameState_GameStarting extends GameState {

    static final int TICK_NEW_GAME_START_LEVEL = 2;
    static final int TICK_NEW_GAME_SHOW_GUYS = 60;
    static final int TICK_NEW_GAME_START_PLAYING = 240;

    public ArcadeGameState_GameStarting() {
        super(CommonGameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.hudState().hideCredit().showLivesCounter();
        gameContext.gamePlay().resetForNewGame(gameContext);
        gameContext.gamePlay().buildNormalLevel(gameContext, 1, gameContext.model().initialLifeCount());
        gameContext.eventManager().publishGameEvent(new GameStartedEvent(gameContext));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final long tick = timer().tickCount();

        if (tick == TICK_NEW_GAME_START_LEVEL) {
            gameContext.gamePlay().startLevel(gameContext);
            // Note: This event is very important because it triggers the creation of the actor animations!
            gameContext.eventManager().publishGameEvent(new LevelStartedEvent(level));
        }
        else if (tick == TICK_NEW_GAME_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
        }
        else if (tick == TICK_NEW_GAME_START_PLAYING) {
            model.setPlaying(true);
            gameContext.flow().enterState(gameContext, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        gameContext.coinMechanism().consumeCoin();
    }
}
