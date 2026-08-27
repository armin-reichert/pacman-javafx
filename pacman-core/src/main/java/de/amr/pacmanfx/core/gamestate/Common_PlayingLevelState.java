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
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gameplay.hunt.GamePlayStep;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.rules.GameRules;
import org.tinylog.Logger;

import java.util.List;

public final class Common_PlayingLevelState extends GameState {

    private GameRules rules;
    private GameFlowController gameFlow;
    private GamePlay gamePlay;
    private GameSystems systems;
    private GameSession session;
    private GameLevel level;
    private Pac pac;

    public Common_PlayingLevelState() {
        super(CommonGameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext game) {
        rules = game.variant().rules();
        gameFlow = game.variant().gameFlow();
        gamePlay = game.variant().gamePlay();
        systems = game.variant().systems();
        session = game.session();
        level = game.session().level();
        pac = level.entities().pac();

        final MessageView messageView = session.hud().messageView();
        if (messageView.data().messageType() == MessageType.READY) {
            session.hud().clearMessage();
        }

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        systems.pacState().setState(pac, PacState.ACTIVE);

        // This call fires a game event!
        level.huntingTimerStrategy().startFirstPhase(game, level.number());
    }

    @Override
    public void onUpdate(GameContext game) {

        gamePlay.update(game, level);
        session.cheats().update(game);

        final GamePlayStep step = session.thisFrame().gamePlayStep();
        logGamePlayStep(step);

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
            Logger.info("--- Game play step:");
            for (String line : report) {
                Logger.info("    - " + line);
            }
        }
    }
}
