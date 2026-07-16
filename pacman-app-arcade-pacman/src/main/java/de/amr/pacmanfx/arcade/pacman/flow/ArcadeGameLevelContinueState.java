/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameContinuedEvent;
import de.amr.pacmanfx.core.flow.GameFlowController;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameLevelContinueState extends GameState {

    static final int TICK_RESUME_HUNTING = 120;
    static final int TICK_CONTINUE_LEVEL = 60;

    public ArcadeGameLevelContinueState() {
        super(GameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        gameContext.gamePlay().prepareLevelForPlaying(gameContext);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        gameContext.gamePlay().showLevelMessage(level, GameLevelMessageType.READY);
        model.hudState().hideCredit().showLivesCounter();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameFlowController gameFlow = gameContext.flow();
        final long tick = timer().tickCount();

        if (tick == TICK_CONTINUE_LEVEL) {
            gameContext.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == TICK_RESUME_HUNTING) {
            gameFlow.enterState(gameContext, GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
