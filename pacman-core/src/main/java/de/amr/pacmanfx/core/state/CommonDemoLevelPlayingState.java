/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;


import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.rules.GameRules;
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
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        model.setLevel(gameContext.gamePlay().buildDemoLevel(gameContext));
        gameContext.hudState().showCredit().hideLivesCounter();
        gameContext.eventManager().publishGameEvent(new LevelCreatedEvent(model.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = model.assertLevel();

        final long tick = timer().tickCount();
        if (tick == 1) {
            gameContext.gamePlay().prepareLevelForPlaying(gameContext);
            gameContext.gamePlay().showLevelMessage(level, GameLevelMessageType.GAME_OVER);
            model.score().setEnabled(false);
            model.highScore().setEnabled(false);
            Logger.info("Demo level {} started", level.number());
            // Note: This event is very important because it triggers the creation of the actor animations!
            gameContext.eventManager().publishGameEvent(new LevelStartedEvent(level));
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
            gameContext.gamePlay().hunt(gameContext);
            gameContext.flow().enterState(gameContext, computeNextState(gameContext));
        }
    }

    private GameStateID computeNextState(GameContext gameContext) {
        final GameLevel level = gameContext.assertLevel();
        final GameRules rules = gameContext.model().rules();

        if (rules.isLevelCompleted(level)) {
            return GameStateID.GAME_INTRO;
        }
        else if (gameContext.thisFrame().huntingStep().pacKilled()) {
            return GameStateID.GAME_LEVEL_PACMAN_DYING;
        }
        else if (gameContext.thisFrame().huntingStep().hasGhostBeenKilled()) {
            return GameStateID.GAME_LEVEL_EATING_GHOST;
        }
        return GameStateID.DEMO_LEVEL_PLAYING;
    }
}
