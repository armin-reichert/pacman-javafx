/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.model.rules.GameRules;
import org.tinylog.Logger;

import java.util.List;

public final class Common_PlayingLevelState extends GameState {

    public Common_PlayingLevelState() {
        super(CommonGameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = game.session().level();
        final Pac pac = level.entities().pac();

        final MessageView messageView = session.hud().messageView();
        if (messageView.data().messageType() == MessageType.READY) {
            session.hud().clearMessage();
        }

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        systems.pacState().setState(pac, PacState.ACTIVE);

        systems.actorSpriteAnimController().playSelected(pac);
        level.entities().ghosts().forEach(systems.actorSpriteAnimController()::playSelected);

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
            gameFlow.enterGameState(game, CommonGameStateID.GAME_LEVEL_COMPLETE);
        }
        else if (step.pacKilled()) {
            gameFlow.enterGameState(game, CommonGameStateID.GAME_LEVEL_PACMAN_DYING);
        }
        else if (step.hasGhostBeenKilled()) {
            gameFlow.enterGameState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
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
