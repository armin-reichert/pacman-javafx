/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

public class GameStartingState extends GameState {

    public GameStartingState() {
        super(GameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameContext.model();
        gameModel.resetForNewGame();
        gameModel.buildNormalLevel(gameContext, gameModel.startLevelNumber());
        gameContext.flow().publishGameEvent(new GameStartedEvent(gameContext));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final long tick = timer().tickCount();

        if (tick == TengenMsPacMan_GameState.Timing.TICK_SHOW_READY) {
            gameModel.startLevel(gameContext, level);
        }
        else if (tick == TengenMsPacMan_GameState.Timing.TICK_NEW_GAME_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == TengenMsPacMan_GameState.Timing.TICK_NEW_GAME_START_HUNTING) {
            gameModel.setPlaying(true);
            gameContext.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
