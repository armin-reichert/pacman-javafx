/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.gameplay.hunt.HuntingStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.GameSession;
import org.tinylog.Logger;

import java.util.List;

public final class GameState_PlayingLevel extends GameState {

    public GameState_PlayingLevel() {
        super(CommonGameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSystems systems = game.variantConfig().systems();
        final GameLevel level = game.session().assertLevel();
        final Pac pac = level.entities().pac();

        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        systems.spriteAnim().playSelected(pac);
        level.entities().ghosts().forEach(systems.spriteAnim()::playSelected);

        // This call fires a game event!
        level.huntingTimerStrategy().startFirstPhase(game, level.number());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        game.variantConfig().gamePlay().hunt(game, level);

        final HuntingStep huntingStep = game.session().thisFrame().huntingStep();
        logHuntingStepResult(huntingStep);

        session.cheats().update(game);

        if (game.variantConfig().rules().isLevelCompleted(level)) {
            session.gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_COMPLETE);
        }
        else if (huntingStep.pacKilled()) {
            session.gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (huntingStep.hasGhostBeenKilled()) {
            session.gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        }
    }

    private void logHuntingStepResult(HuntingStep result) {
        final List<String> report = result.asText();
        if (!report.isEmpty()) {
            Logger.info("Hunting Step:");
            for (String line : report) {
                Logger.info("- " + line);
            }
        }
    }
}
