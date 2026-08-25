/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.event.gameplay.GameContinuedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.LevelMessageType;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.model.rules.LevelContinuationRules;

public class Common_LevelContinueState extends GameState {

    public Common_LevelContinueState() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.level();

        game.variant().gamePlay().prepareLevelForPlaying(game);

        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        game.variant().gamePlay().showLevelMessage(game, level, LevelMessageType.READY);
    }

    @Override
    public void onUpdate(GameContext game) {
        final LevelContinuationRules rules = game.variant().rules().levelContinuation();
        final long tick = timer().tickCount();

        game.variant().systems().entityUpdater().updateHUD(game);

        if (tick == rules.continuePlayingTicks()) {
            game.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == rules.resumeHuntingTicks()) {
            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
