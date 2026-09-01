/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.gameplay.GameContinuedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.rules.LevelContinuationRules;

public class Common_LevelContinueState extends AbstractGameState {

    private GameLevel level;

    public Common_LevelContinueState() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = session.level();
        gamePlay.prepareLevelForPlaying(game, level);
        gamePlay.showMessage(game, MessageType.READY);
        level.entities().pac().state().setEnumValue(PacState.SLEEPING);
        timer().restartIndefinitely();
    }

    @Override
    public void onUpdate(GameContext game) {
        final LevelContinuationRules continuationRules = rules.levelContinuation();
        final long tick = timer().tickCount();
        if (tick == 1) {
            showPacAndGhosts(level.entities());
            lockPacAndGhosts(level.entities(), true);
        }
        else if (tick == continuationRules.continuePlayingTicks()) {
            game.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == continuationRules.resumeHuntingTicks()) {
            lockPacAndGhosts(level.entities(), false);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
