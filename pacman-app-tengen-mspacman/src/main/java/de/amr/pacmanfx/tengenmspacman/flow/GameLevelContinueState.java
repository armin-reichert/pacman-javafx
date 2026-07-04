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
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.assertLevel();

        gameModel.prepareLevelForPlaying(level);
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        gameContext.flow().publishGameEvent(new GameContinuedEvent(gameContext));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final long tick = timer().tickCount();
        if (tick == TICK_RESUME_HUNTING) {
            gameContext.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
