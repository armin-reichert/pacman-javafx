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
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.rules.LevelContinuationRules;

public class Common_LevelContinueState extends GameState {

    public Common_LevelContinueState() {
        super(CommonGameStateID.GAME_LEVEL_CONTINUE);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.level();

        game.variant().gamePlay().prepareLevelForPlaying(game, level);

        // Initially, ghosts and Pac-Man do not move nor animate
        final Pac pac = level.entities().pac();
        pac.show();
        game.variant().systems().worldNavigator().setDisabled(pac, true);
        game.variant().systems().pacAnimation().setDisabled(pac, true);

        for (Ghost ghost : level.entities().ghosts()) {
            ghost.show();
            game.variant().systems().worldNavigator().setDisabled(ghost, true);
            game.variant().systems().ghostAnimation().setDisabled(ghost, true);
        }

        game.variant().gamePlay().showMessage(game, MessageType.READY);
    }

    @Override
    public void onUpdate(GameContext game) {
        final LevelContinuationRules rules = game.variant().rules().levelContinuation();
        final long tick = timer().tickCount();
        final GameSession session = game.session();
        final GameLevel level = session.level();

        if (tick == rules.continuePlayingTicks()) {
            game.eventManager().publishGameEvent(new GameContinuedEvent());
        }
        else if (tick == rules.resumeHuntingTicks()) {
            // Initially, ghosts and Pac-Man do not move nor animate
            final Pac pac = level.entities().pac();
            game.variant().systems().worldNavigator().setDisabled(pac, false);
            game.variant().systems().pacAnimation().setDisabled(pac, false);

            for (Ghost ghost : level.entities().ghosts()) {
                game.variant().systems().worldNavigator().setDisabled(ghost, false);
                game.variant().systems().ghostAnimation().setDisabled(ghost, false);
            }

            game.variant().gameFlow().enterGameState(game, CommonGameStateID.GAME_LEVEL_PLAYING);
        }
    }
}
