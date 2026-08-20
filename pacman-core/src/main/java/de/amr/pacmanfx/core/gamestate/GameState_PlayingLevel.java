/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.model.rules.GameRules;
import org.tinylog.Logger;

import java.util.List;

public final class GameState_PlayingLevel extends GameState {

    public GameState_PlayingLevel() {
        super(CommonGameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = game.session().level();
        final Pac pac = level.entities().pac();

        session.hud().optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> session.hud().clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        systems.spriteAnimController().playSelected(pac);
        level.entities().ghosts().forEach(systems.spriteAnimController()::playSelected);

        // This call fires a game event!
        level.huntingTimerStrategy().startFirstPhase(game, level.number());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();
        final GameFlowController gameFlow = game.variant().gameFlow();

        systems.entityUpdater().updateEntities(game, level);

        game.variant().gamePlay().updateGamePlay(game, level);

        final GamePlayStep step = session.thisFrame().gamePlayStep();
        logGamePlayStep(step);

        session.cheats().update(game);

        if (rules.isLevelCompleted(level)) {
            gameFlow.enterState(game, CommonGameStateID.GAME_LEVEL_COMPLETE);
        }
        else if (step.pacKilled()) {
            gameFlow.enterState(game, CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (step.hasGhostBeenKilled()) {
            gameFlow.enterState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
        }
    }

    private void logGamePlayStep(GamePlayStep result) {
        final List<String> report = result.asText();
        if (!report.isEmpty()) {
            Logger.info("Hunting Step:");
            for (String line : report) {
                Logger.info("- " + line);
            }
        }
    }
}
