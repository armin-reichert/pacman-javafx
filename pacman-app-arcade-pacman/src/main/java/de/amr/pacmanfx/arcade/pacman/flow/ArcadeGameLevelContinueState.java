/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

public class ArcadeGameLevelContinueState extends GameState {

    public ArcadeGameLevelContinueState() {
        super(GameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();

        gameModel.prepareLevelForPlaying(level);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        gameModel.showLevelMessage(level, GameLevelMessageType.READY);
        gameModel.hudState().creditOff().livesCounterOn();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlow flow = gameContext.flow();
        final long tick = timer().tickCount();

        if (tick == Arcade_GameState.Timing.TICK_CONTINUE_LEVEL) {
            flow.publishGameEvent(new GameContinuedEvent(gameContext));
        }
        else if (tick == Arcade_GameState.Timing.TICK_RESUME_HUNTING) {
            flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
