/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.gameplay.HuntingStepResult;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.session.GameSession;
import org.tinylog.Logger;

import java.util.List;

public final class GameState_PlayingLevel extends GameState {

    public GameState_PlayingLevel() {
        super(CommonGameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSystems sys = game.systems();
        final GameLevel level = game.session().assertLevel();
        final Pac pac = level.entities().pac();

        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        sys.spriteAnim().playSelected(pac);
        level.entities().ghosts().forEach(sys.spriteAnim()::playSelected);

        // This call fires a game event!
        level.huntingTimerStrategy().startFirstPhase(game, level.number());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();

        game.gamePlay().hunt(game, level);
        logHuntingStepResult(game.thisFrame().huntingStep());

        game.cheats().update(game);
        game.flow().enterState(game, computeNextState(game));
    }

    private CommonGameStateID computeNextState(GameContext game) {
        final GameLevel level = game.session().assertLevel();
        final GameRules rules = game.model().rules();

        if (rules.isLevelCompleted(level)) {
            return CommonGameStateID.GAME_LEVEL_COMPLETE;
        }
        else if (game.thisFrame().huntingStep().pacKilled()) {
            return CommonGameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (game.thisFrame().huntingStep().hasGhostBeenKilled()) {
            return CommonGameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return CommonGameStateID.GAME_LEVEL_PLAYING;
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
