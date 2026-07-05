/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameContinuedEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;

public class GameLevelContinueState extends GameState {

    static final short TICK_RESUME_HUNTING = 240;

    public GameLevelContinueState() {
        super(GameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();

        context.gamePlay().prepareLevelForPlaying(level);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        context.eventManager().publishGameEvent(new GameContinuedEvent());
    }

    @Override
    public void onUpdate(GameContext context) {
        final long tick = timer().tickCount();
        if (tick == TICK_RESUME_HUNTING) {
            context.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
