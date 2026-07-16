/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;


import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.GameRules;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
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
        model.setLevel(context.gamePlay().buildDemoLevel(context));
        model.hudState().showCredit().hideLivesCounter();
        context.eventManager().publishGameEvent(new LevelCreatedEvent(model.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();

        final long tick = timer().tickCount();
        if (tick == 1) {
            context.gamePlay().prepareLevelForPlaying(level);
            context.gamePlay().showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            model.score().setEnabled(false);
            model.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
            // Note: This event is very important because it triggers the creation of the actor animations!
            context.eventManager().publishGameEvent(new LevelStartedEvent(level));
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
            context.gamePlay().hunt(context);
            context.flow().enterState(computeNextState(context));
        }
    }

    private GameStateID computeNextState(GameContext context) {
        final GameLevel level = context.level();
        final GameRules rules = context.model().rules();

        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_INTRO;
        }
        else if (context.thisFrame().huntingStepResult().pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (context.thisFrame().huntingStepResult().hasGhostBeenKilled()) {
            return GameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return GameStateID.DEMO_LEVEL_PLAYING;
    }
}
