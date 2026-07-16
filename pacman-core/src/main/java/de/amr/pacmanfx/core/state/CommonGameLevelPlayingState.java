/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.GameRules;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.simulation.HuntingStepResult;
import org.tinylog.Logger;

import java.util.List;

public class CommonGameLevelPlayingState extends GameState {

    public CommonGameLevelPlayingState() {
        super(GameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameLevel level = context.model().assertLevel();

        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        level.entities().pac().animations().playSelected();
        level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());

        // This call fires a game event!
        level.huntingTimer().startFirstPhase(context, level.number());
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();

        context.gamePlay().hunt(context);
        logHuntingStepResult(context.thisFrame().huntingStepResult());

        context.cheats().update(level);
        context.flow().enterState(computeNextState(context));
    }

    private GameStateID computeNextState(GameContext context) {
        final GameLevel level = context.level();
        final GameRules rules = context.model().rules();

        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_LEVEL_COMPLETE;
        }
        else if (context.thisFrame().huntingStepResult().pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (context.thisFrame().huntingStepResult().hasGhostBeenKilled()) {
            return GameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return GameStateID.GAME_LEVEL_PLAYING;
    }

    private void logHuntingStepResult(HuntingStepResult result) {
        final List<String> report = result.asText();
        if (!report.isEmpty()) {
            Logger.info("Hunting Step:");
            for (String line : report) {
                Logger.info("- " + line);
            }
        }
    }
}
