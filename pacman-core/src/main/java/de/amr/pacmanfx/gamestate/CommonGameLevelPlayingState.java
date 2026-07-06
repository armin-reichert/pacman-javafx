/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.simulation.HuntingStepResult;
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
        model.setHuntingStepResult(context.gamePlay().hunt(context, context.eventManager(), level));
        context.cheats().update(level);
        logHuntingStepResult(model.huntingStepResult());
        context.flow().enterState(computeNextState(model.huntingStepResult(), level));
    }

    private GameStateID computeNextState(HuntingStepResult result, GameLevel level) {
        final GameRules rules = level.gameModel().rules();
        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_LEVEL_COMPLETE;
        }
        else if (result.pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (result.hasGhostBeenKilled()) {
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
