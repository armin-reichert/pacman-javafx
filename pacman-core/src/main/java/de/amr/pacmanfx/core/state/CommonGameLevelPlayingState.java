/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gameplay.HuntingStepResult;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.rules.GameRules;
import org.tinylog.Logger;

import java.util.List;

public class CommonGameLevelPlayingState extends GameState {

    public CommonGameLevelPlayingState() {
        super(GameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final GameLevel level = gameContext.model().assertLevel();
        final Pac pac = level.entities().pac();

        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        sys.spriteAnim().playSelected(pac);
        level.entities().ghosts().forEach(sys.spriteAnim()::playSelected);

        // This call fires a game event!
        level.huntingRules().startFirstPhase(gameContext, level.number());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = model.assertLevel();

        gameContext.gamePlay().hunt(gameContext);
        logHuntingStepResult(gameContext.thisFrame().huntingStep());

        gameContext.cheats().update(level);
        gameContext.flow().enterState(gameContext, computeNextState(gameContext));
    }

    private GameStateID computeNextState(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final GameRules rules = gameContext.model().rules();

        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_LEVEL_COMPLETE;
        }
        else if (gameContext.thisFrame().huntingStep().pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (gameContext.thisFrame().huntingStep().hasGhostBeenKilled()) {
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
