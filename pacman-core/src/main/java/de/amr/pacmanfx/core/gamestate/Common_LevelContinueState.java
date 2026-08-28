/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.event.gameplay.GameContinuedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.rules.LevelContinuationRules;

public class Common_LevelContinueState extends AbstractGameState {

    public Common_LevelContinueState() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnterState(GameContext game) {
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();

        gamePlay.prepareLevelForPlaying(game, level);

        // Initially, ghosts and Pac-Man do not move nor animate
        pac.show();
        systems.worldNavigator().setDisabled(pac, true);
        systems.pacAnimation().setDisabled(pac, true);

        for (Ghost ghost : level.entities().ghosts()) {
            ghost.show();
            systems.worldNavigator().setDisabled(ghost, true);
            systems.ghostAnimation().setDisabled(ghost, true);
        }

        gamePlay.showMessage(game, MessageType.READY);
    }

    @Override
    public void onUpdate(GameContext game) {
        final LevelContinuationRules continuationRules = rules.levelContinuation();
        final long tick = timer().tickCount();
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();

        if (tick == continuationRules.continuePlayingTicks()) {
            game.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == continuationRules.resumeHuntingTicks()) {
            // Initially, ghosts and Pac-Man do not move nor animate
            systems.worldNavigator().setDisabled(pac, false);
            systems.pacAnimation().setDisabled(pac, false);

            for (Ghost ghost : level.entities().ghosts()) {
                systems.worldNavigator().setDisabled(ghost, false);
                systems.ghostAnimation().setDisabled(ghost, false);
            }

            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
