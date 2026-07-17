/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameStartedEvent;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameStartingState extends GameState {

    static final int TICK_NEW_GAME_START_LEVEL = 2;
    static final int TICK_NEW_GAME_SHOW_GUYS = 60;
    static final int TICK_NEW_GAME_START_PLAYING = 240;

    public ArcadeGameStartingState() {
        super(GameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        gameContext.model().hudState().hideCredit().showLivesCounter();
        gameContext.gamePlay().resetForNewGame(gameContext);
        gameContext.gamePlay().buildNormalLevel(gameContext, 1);
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
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == TICK_NEW_GAME_START_PLAYING) {
            model.setPlaying(true);
            gameContext.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        gameContext.coinMechanism().consumeCoin();
    }
}
