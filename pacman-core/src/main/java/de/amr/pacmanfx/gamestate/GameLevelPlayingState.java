/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.HuntingPhaseStartedEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.simulation.EntityCollisionDetector;
import de.amr.pacmanfx.simulation.EntityCollisionResolver;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import org.tinylog.Logger;

import java.util.List;

public class GameLevelPlayingState extends GameState {

    public GameLevelPlayingState() {
        super(GameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();

        // Clear "READY!" message. "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared!
        level.optMessage()
            .filter(message -> message.type() == GameLevelMessageType.READY)
            .ifPresent(_ -> level.clearMessage());

        level.heartbeat().setStartState(Pulse.State.ON);
        level.heartbeat().restart();

        level.entities().pac().animations().playSelected();
        level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());

        final HuntingTimer huntingTimer = level.huntingTimer();
        huntingTimer.startFirstPhase(gameContext.rules(), level.number());

        gameContext.flow().publishGameEvent(new HuntingPhaseStartedEvent(
            gameContext,
            huntingTimer.phaseIndex(),
            huntingTimer.currentHuntingPhase())
        );
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = gameModel.gateKeeper();
        final boolean doubleChecked = gameContext.rules().collisionDoubleCheckedProperty().get();

        // Update
        gameModel.cheats().update(level);
        level.heartbeat().triggerPulse();
        level.huntingTimer().update(gameContext.rules(), level.number());
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        gameModel.updatePacPowerMode(gameContext, level, pac);

        final EntityCollisionDetector collisionDetector = new EntityCollisionDetector(gameContext);

        // If double-check active, do an additional collision check before Pac has moved
        level.entities().forEach(entity -> {
            if (entity != pac) {
                entity.update(gameContext, level);
            }
        });
        if (doubleChecked) {
            collisionDetector.detectCollisions(level);
        }
        pac.update(gameContext, level);
        collisionDetector.detectCollisions(level);

        final EntityCollisionResolver collisionResolver = new EntityCollisionResolver(gameContext);
        collisionResolver.evaluateCollisions(level);

        writeResultToLog(gameContext.huntingStepResult());

        // State transition
        final GameStateID nextStateID = computeNextState(gameContext.huntingStepResult(), gameContext.rules(), level);
        gameContext.flow().enterState(nextStateID.name());
    }

    private GameStateID computeNextState(HuntingStepResult result, GameRules rules, GameLevel level) {
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

    private void writeResultToLog(HuntingStepResult result) {
        final List<String> report = result.asText();
        if (!report.isEmpty()) {
            Logger.info("Hunting Step:");
            for (String line : report) {
                Logger.info("- " + line);
            }
        }
    }
}
