/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;


import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.LevelCreatedEvent;
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

public class CommonDemoLevelPlayingState extends GameState {

    private final int huntingStartTick;

    public CommonDemoLevelPlayingState(int huntingStartTick) {
        super(GameStateID.DEMO_LEVEL_PLAYING);
        this.huntingStartTick = huntingStartTick;
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        model.setLevel(model.buildDemoLevel());
        model.hudState().creditOn().livesCounterOff();
        context.flow().publishGameEvent(new LevelCreatedEvent(context, model.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext context) {
        final long tick = timer().tickCount();
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();

        if (tick == 1) {
            model.prepareLevelForPlaying(level);
            model.showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            model.score().setEnabled(false);
            model.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
            // Note: This event is very important because it triggers the creation of the actor animations!
            context.flow().publishGameEvent(new LevelStartedEvent(context, level));
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
            level.huntingTimer().startFirstPhase(context, level.number());
        }
        else if (tick > huntingStartTick) {
            hunt(context);
            final GameStateID nextStateID = computeNextState(context.huntingStepResult(), model.rules(), level);
            context.flow().enterState(nextStateID);
        }
    }

    private void hunt(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();
        final Pac pac = level.entities().pac();
        final GateKeeper gateKeeper = model.gateKeeper();
        final boolean doubleChecked = model.rules().collisionDoubleCheckedProperty().get();

        level.heartbeat().triggerPulse();

        level.huntingTimer().update(model.rules(), level.number());

        if (gateKeeper != null) {
            gateKeeper.unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }

        model.updatePacPowerMode(context, level, pac);

        final EntityCollisionDetector collisionDetector = new EntityCollisionDetector(context);

        // If double-check active, do an additional collision check before Pac has moved
        level.entities().forEach(entity -> {
            if (entity != pac) {
                entity.update(context, level);
            }
        });
        if (doubleChecked) {
            collisionDetector.detectCollisions(level);
        }
        pac.update(context, level);
        collisionDetector.detectCollisions(level);

        final EntityCollisionResolver collisionResolver = new EntityCollisionResolver(context);
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
