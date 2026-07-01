/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.simulation.HuntingResolver;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import org.tinylog.Logger;

import static de.amr.pacmanfx.simulation.HuntingCollisionDetector.detectCollisions;

public class GameLevelPlayingState extends GameState {

    public GameLevelPlayingState() {
        super(GameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();
        gameModel.onStartLevelPlaying(gameContext, level);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = gameModel.gateKeeper();

        // Update
        gameModel.cheats().update(level);
        level.heartbeat().triggerPulse();
        level.huntingTimer().update(gameContext.rules(), level.number());
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        gameModel.updatePacPowerMode(gameContext, level, pac);

        gameContext.startNewHuntingStep();

        level.entities().forEach(entity -> {
            if (entity != level.entities().pac()) {
                entity.update(gameContext, level);
            }
        });
        if (gameContext.rules().collisionDoubleCheckedProperty().get()) {
            detectCollisions(gameContext);
        }
        level.entities().pac().update(gameContext, level);
        detectCollisions(gameContext);

        // Resolving
        HuntingResolver.evaluate(gameContext);

        logHuntingStep(gameContext);

        // State transition
        final GameStateID nextStateID = computeNextState(gameContext.huntingResult(), gameContext.rules(), level);
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


    private void logHuntingStep(GameContext context) {
        final var report = HuntingStepResult.createReport(context.huntingResult());
        if (!report.isEmpty()) {
            Logger.info("Hunting Step:");
            for (var msg : report) {
                Logger.info("- " + msg);
            }
        }
    }
}
