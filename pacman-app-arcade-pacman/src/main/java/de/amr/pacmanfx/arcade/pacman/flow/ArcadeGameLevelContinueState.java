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

        model.showLevelMessage(level, GameLevelMessageType.READY);
        model.hudState().creditOff().livesCounterOn();
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameFlow flow = context.flow();
        final long tick = timer().tickCount();

        if (tick == TICK_CONTINUE_LEVEL) {
            flow.publishGameEvent(new GameContinuedEvent(context));
        }
        else if (tick == TICK_RESUME_HUNTING) {
            flow.enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
