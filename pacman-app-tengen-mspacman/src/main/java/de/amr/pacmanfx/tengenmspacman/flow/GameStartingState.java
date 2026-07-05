/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameStartedEvent;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

public class GameStartingState extends GameState {

    static final short TICK_SHOW_READY = 10;
    static final short TICK_SHOW_GUYS = 70;
    static final short TICK_START_HUNTING = 250;

    public GameStartingState() {
        super(GameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext context) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) context.model();
        context.gamePlay().resetForNewGame(context);
        context.gamePlay().buildNormalLevel(context, model.startLevelNumber());
        context.flow().publishGameEvent(new GameStartedEvent(context));
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();
        final long tick = timer().tickCount();

        if (tick == TICK_SHOW_READY) {
            context.gamePlay().startLevel(context, level);
            // Note: This event is very important because it triggers the creation of the actor animations!
            context.flow().publishGameEvent(new LevelStartedEvent(context, level));
        }
        else if (tick == TICK_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == TICK_START_HUNTING) {
            model.setPlaying(true);
            context.flow().enterState(GameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
