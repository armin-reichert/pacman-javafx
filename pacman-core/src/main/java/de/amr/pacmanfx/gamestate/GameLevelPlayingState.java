/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.simulation.HuntingCollisionDetector;
import de.amr.pacmanfx.simulation.HuntingResolver;
import de.amr.pacmanfx.simulation.HuntingStateTransitions;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import org.tinylog.Logger;

public class GameLevelPlayingState extends GameState {

    public GameLevelPlayingState() {
        super(GameStateID.GAME_LEVEL_PLAYING);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        game.onStartLevelPlaying(context, level);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.gameModel();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = gameModel.gateKeeper();

        // Update
        gameModel.cheats().update(level);
        level.heartbeat().triggerPulse();
        level.huntingTimer().update(gameContext.gameRules(), level.number());
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        gameModel.updatePacPowerMode(gameContext, level, pac);

        gameContext.startNewHuntingStep();
        if (gameContext.isCollisionDoubleChecked()) {
            HuntingCollisionDetector.detectCollisions(gameContext);
        }
        level.entities().forEach(entity -> entity.update(gameContext, level));
        HuntingCollisionDetector.detectCollisions(gameContext);

        // Resolving
        HuntingResolver.evaluate(gameContext);

        logHuntingStep(gameContext);

        // State transition
        final GameStateID nextStateID = HuntingStateTransitions
            .computeNextState(gameContext.huntingResult(), gameContext.gameRules(), level);
        gameContext.gameFlow().enterState(nextStateID.name());
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
