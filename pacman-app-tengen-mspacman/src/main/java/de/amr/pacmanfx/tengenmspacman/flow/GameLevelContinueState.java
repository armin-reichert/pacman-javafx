/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameContinuedEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class GameLevelContinueState extends GameState {

    static final short TICK_RESUME_HUNTING = 240;

    public GameLevelContinueState() {
        super(GameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = model.assertLevel();

        gameContext.gamePlay().prepareLevelForPlaying(gameContext);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        gameContext.eventManager().publishGameEvent(new GameContinuedEvent());
    }

    @Override
    public void onUpdate(GameContext context) {
        final long tick = timer().tickCount();
        if (tick == TICK_RESUME_HUNTING) {
            context.flow().enterState(context, GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
