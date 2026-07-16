/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameStartedEvent;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameStartingState extends GameState {

    static final int TICK_NEW_GAME_START_LEVEL = 2;
    static final int TICK_NEW_GAME_SHOW_GUYS = 60;
    static final int TICK_NEW_GAME_START_HUNTING = 240;

    public ArcadeGameStartingState() {
        super(GameStateID.GAME_STARTING);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();

        model.hudState().hideCredit().showLivesCounter();

        context.gamePlay().resetForNewGame(model);
        context.gamePlay().buildNormalLevel(context, 1);

        context.eventManager().publishGameEvent(new GameStartedEvent(context));
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = context.model().assertLevel();
        final long tick = timer().tickCount();

        if (tick == TICK_NEW_GAME_START_LEVEL) {
            context.gamePlay().startLevel(context);
            // Note: This event is very important because it triggers the creation of the actor animations!
            context.eventManager().publishGameEvent(new LevelStartedEvent(level));
        }
        else if (tick == TICK_NEW_GAME_SHOW_GUYS) {
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == TICK_NEW_GAME_START_HUNTING) {
            model.setPlaying(true);
            context.flow().enterState(context, GameStateID.GAME_LEVEL_PLAYING);
        }
    }

    @Override
    public void onExit(GameContext context) {
        context.coinMechanism().consumeCoin();
    }
}
