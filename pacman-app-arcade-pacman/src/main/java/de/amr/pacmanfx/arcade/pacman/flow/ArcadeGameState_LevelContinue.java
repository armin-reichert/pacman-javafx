/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.event.gameplay.GameContinuedEvent;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.GameSession;

public class ArcadeGameState_LevelContinue extends GameState {

    static final int TICK_RESUME_HUNTING = 120;
    static final int TICK_CONTINUE_LEVEL = 60;

    public ArcadeGameState_LevelContinue() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        game.variant().gamePlay().prepareLevelForPlaying(game);
        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        game.variant().gamePlay().showLevelMessage(game, level, GameLevelMessageType.READY);
        session.hud().hideCredit().showLivesCounter();
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameFlowController gameFlow = game.variant().gameFlow();
        final long tick = timer().tickCount();

        if (tick == TICK_CONTINUE_LEVEL) {
            game.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == TICK_RESUME_HUNTING) {
            gameFlow.enterState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
