/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller.teststates;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.AnimationManager;

public class LevelMediumTestState implements GameState {
    static final int TEST_DURATION_SEC = 10;

    private final TickTimer timer = new TickTimer("Timer_" + name());
    private int lastTestedLevelNumber;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    private void configureLevelForTest(GameContext context) {
        final GameLevel gameLevel = context.gameLevel();
        gameLevel.pac().usingAutopilotProperty().unbind();
        gameLevel.pac().setUsingAutopilot(true);
        gameLevel.pac().animations().ifPresent(AnimationManager::play);
        gameLevel.ghosts().forEach(ghost -> ghost.animations().ifPresent(AnimationManager::play));
        gameLevel.showPacAndGhosts();
        GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
        message.setPosition(gameLevel.defaultMessagePosition());
        gameLevel.setMessage(message);
        context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public void onEnter(GameContext context) {
        lastTestedLevelNumber = context.game().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.game().lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        context.game().prepareForNewGame();
        context.game().buildNormalLevel(1);
        context.game().startLevel();
        configureLevelForTest(context);
    }

    @Override
    public void onUpdate(GameContext context) {
        context.game().doHuntingStep(context);
        if (timer().hasExpired()) {
            if (context.gameLevel().number() == lastTestedLevelNumber) {
                context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
                context.gameController().changeGameState(GamePlayState.INTRO);
            } else {
                timer().restartSeconds(TEST_DURATION_SEC);
                context.game().startNextLevel();
                configureLevelForTest(context);
            }
        }
        else if (context.game().isLevelCompleted()) {
            context.gameController().changeGameState(GamePlayState.INTRO);
        } else if (context.game().hasPacManBeenKilled()) {
            timer.expire();
        } else if (context.game().haveGhostsBeenKilled()) {
            context.gameController().changeGameState(GamePlayState.GHOST_DYING);
        }
    }

    @Override
    public void onExit(GameContext context) {
        context.game().hudData().levelCounter().clear();
    }
}
