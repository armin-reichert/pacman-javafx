/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.level.GameLevel;
import org.tinylog.Logger;

import java.util.List;

public final class Common_PlayingLevelState extends AbstractGameState {

    private GameLevel level;

    public Common_PlayingLevelState() {
        super(CommonGameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnterState(GameContext game) {
        level = game.session().level();

        level.entities().theMessageView().hide();

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        level.entities().pac().state().setEnumValue(PacState.ACTIVE);

        // This call fires a game event!
        level.huntingTimer().startFirstPhase(game, level.number());
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        
        gamePlay.update(game, level);
        session.cheats().update(game);

        logGamePlayStep(session.thisFrame());

        if (rules.isLevelCompleted(level)) {
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_COMPLETE);
        }
        else if (session.thisFrame().pacKilled()) {
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (session.thisFrame().hasGhostBeenKilled()) {
            flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        }
    }

    private static void logGamePlayStep(FrameState frameState) {
        final List<String> report = frameState.asText();
        if (!report.isEmpty()) {
            Logger.info("--- Game play step:");
            for (String line : report) {
                Logger.info("    - " + line);
            }
        }
    }
}
