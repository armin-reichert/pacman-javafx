/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;

public class ArcadeGameStartingState extends GameState {

    public ArcadeGameStartingState() {
        super(GameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();

        gameModel.hudState().creditOff().livesCounterOn();
        gameModel.resetForNewGame();
        gameModel.buildNormalLevel(gameContext, 1);

        flow.publishGameEvent(new GameStartedEvent(gameContext));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final GameModel gameModel = gameContext.model();
        final long tick = timer().tickCount();

        if (tick == Arcade_GameState.Timing.TICK_NEW_GAME_START_LEVEL) {
            gameModel.startLevel(gameContext);
        }
        else if (tick == Arcade_GameState.Timing.TICK_NEW_GAME_SHOW_GUYS) {
            final GameLevel level = gameContext.requireLevel();
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == Arcade_GameState.Timing.TICK_NEW_GAME_START_HUNTING) {
            gameModel.setPlaying(true);
            flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        gameContext.coinMechanism().consumeCoin();
    }

}
