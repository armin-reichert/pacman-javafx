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
        level.showMessage(MessageType.READY);
        level.entities().pac().state().setEnumValue(PacState.SLEEPING);
        timer().restartIndefinitely();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        final LevelContinuationRules continuationRules = rules.levelContinuation();
        if (stateTick == 1) {
            showPacAndGhosts(level.entities());
            lockPacAndGhosts(level.entities(), true);
        }
        else if (stateTick == continuationRules.continuePlayingTicks()) {
            game.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (stateTick == continuationRules.resumeHuntingTicks()) {
            lockPacAndGhosts(level.entities(), false);
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
