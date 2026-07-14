/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameContinuedEvent;
import de.amr.pacmanfx.core.flow.GameFlow;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;

public class ArcadeGameLevelContinueState extends GameState {

    static final int TICK_RESUME_HUNTING = 120;
    static final int TICK_CONTINUE_LEVEL = 60;

    public ArcadeGameLevelContinueState() {
        super(GameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = context.model().assertLevel();

        context.gamePlay().prepareLevelForPlaying(level);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        context.gamePlay().showLevelMessage(level, GameLevelMessageType.READY);
        model.hudState().hideCredit().showLivesCounter();
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameFlow flow = context.flow();
        final long tick = timer().tickCount();

        if (tick == TICK_CONTINUE_LEVEL) {
            context.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == TICK_RESUME_HUNTING) {
            flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
