/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.ui.entities.props.messageview.MessageType;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.actor.pac.PacState;
import de.amr.pacmanfx.core.event.gameplay.GameContinuedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.LevelContinuationRules;

public class Common_LevelContinueState extends AbstractGameState {

    private GameLevel level;

    public Common_LevelContinueState() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void doEnterState(GameContext game) {
        level = game.session().optLevel().orElseThrow();
        game.play().prepareLevelForPlaying(game, level);
        level.showMessage(MessageType.READY);
        level.entitySet().pac().state().setEnumValue(PacState.SLEEPING);
        stateTimer().restartIndefinitely();
    }

    @Override
    public void doUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        final LevelContinuationRules continuationRules = game.rules().levelContinuation();
        if (stateTickCount == 1) {
            level.entitySet().showPacAndGhosts();
            lockPacAndGhosts(game, level.entitySet(), true);
        }
        else if (stateTickCount == continuationRules.continuePlayingTicks()) {
            game.runtime().eventManager().publishEvent(new GameContinuedEvent(game));
        }
        else if (stateTickCount == continuationRules.resumeHuntingTicks()) {
            lockPacAndGhosts(game, level.entitySet(), false);
            game.flow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
