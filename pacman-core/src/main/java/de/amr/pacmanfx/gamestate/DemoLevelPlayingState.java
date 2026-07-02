/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;


import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.GateKeeper;
import de.amr.pacmanfx.simulation.EntityCollisionDetector;
import de.amr.pacmanfx.simulation.EntityCollisionResolver;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import org.tinylog.Logger;

public class DemoLevelPlayingState extends GameState {

    private final int huntingStartTick;

    public DemoLevelPlayingState(int huntingStartTick) {
        super(GameStateID.DEMO_LEVEL_PLAYING);
        this.huntingStartTick = huntingStartTick;
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.buildDemoLevel(gameContext);
        gameModel.hud().creditOn().livesCounterOff();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final long tick = timer().tickCount();
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();

        if (tick == 1) {
            gameModel.prepareLevelForPlaying(level);
            gameModel.showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            gameModel.score().setEnabled(false);
            gameModel.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
            // Note: This event is very important because it triggers the creation of the actor animations!
            gameContext.flow().publishGameEvent(new LevelStartedEvent(gameContext, level));
        }
        else if (tick == 2) {
            // Now, actor animations are available, show them
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
        }
        else if (tick == huntingStartTick) {
            // Clear "READY!" message. "GAME_OVER" (demo level) and  "TEST LEVEL XX" messages are not cleared!
            level.optMessage()
                .filter(message -> message.type() == GameLevelMessageType.READY)
                .ifPresent(_ -> level.clearMessage());

            level.heartbeat().setStartState(Pulse.State.ON);
            level.heartbeat().restart();

            level.entities().pac().animations().playSelected();
            level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());

            // This call fires a game event!
            level.huntingTimer().startFirstPhase(gameContext, level.number());
        }
        else if (tick > huntingStartTick) {
            hunt(gameContext);
            final GameStateID nextStateID = computeNextState(gameContext.huntingStepResult(), gameContext.rules(), level);
            gameContext.flow().enterState(nextStateID);
        }
    }

    private void hunt(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameContext.requireLevel();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = gameModel.gateKeeper();
        final boolean doubleChecked = gameContext.rules().collisionDoubleCheckedProperty().get();

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
    }

    private GameStateID computeNextState(HuntingStepResult result, GameRules rules, GameLevel level) {
        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_INTRO;
        }
        else if (result.pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (result.hasGhostBeenKilled()) {
            return GameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return GameStateID.DEMO_LEVEL_PLAYING;
    }
}
