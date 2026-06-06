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
    public void onUpdate(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = game.gateKeeper();

        // Update
        game.cheats().update(level);
        level.heartbeat().triggerPulse();
        level.huntingTimer().update(context.gameRules(), level.number());
        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        game.updatePacPowerMode(level, pac);

        context.startNewHuntingStep();
        if (context.isCollisionDoubleChecked()) {
            HuntingCollisionDetector.detectCollisions(context);
        }
        level.entities().forEach(entity -> entity.update(level));
        HuntingCollisionDetector.detectCollisions(context);

        // Resolving
        HuntingResolver.evaluate(context);

        logHuntingStep(context);

        // State transition
        final GameStateID nextStateID = HuntingStateTransitions
            .computeNextState(context.huntingResult(), context.gameRules(), level);
        context.gameFlow().enterState(nextStateID.name());
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
