/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.event.gameplay.GameContinuedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;

public class Tengen_LevelContinueState extends GameState {

    static final short TICK_RESUME_HUNTING = 240;

    public Tengen_LevelContinueState() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameLevel level = game.session().level();
        game.variant().gamePlay().prepareLevelForPlaying(game);
        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);
       game.eventManager().publishGameEvent(new GameContinuedEvent());
    }

    @Override
    public void onUpdate(GameContext game) {
        final long tick = timer().tickCount();

        game.variant().systems().entityUpdater().updateSessionHUDEntities(game);

        if (tick == TICK_RESUME_HUNTING) {
            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
