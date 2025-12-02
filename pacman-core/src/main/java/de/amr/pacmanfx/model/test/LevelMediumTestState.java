/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.AnimationManager;

public class LevelMediumTestState implements StateMachine.State<Game>, TestState {
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

    private void configureLevelForTest(Game game) {
        final GameLevel gameLevel = game.level();
        gameLevel.pac().usingAutopilotProperty().unbind();
        gameLevel.pac().setUsingAutopilot(true);
        gameLevel.pac().optAnimationManager().ifPresent(AnimationManager::play);
        gameLevel.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        gameLevel.showPacAndGhosts();
        GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
        message.setPosition(gameLevel.worldMap().terrainLayer().messageCenterPosition());
        gameLevel.setMessage(message);
        game.hud().creditVisible(false);
        game.publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
    }

    @Override
    public void onEnter(Game game) {
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        game.prepareForNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        configureLevelForTest(game);
    }

    @Override
    public void onUpdate(Game game) {
        game.level().pac().tick(game);
        game.level().ghosts().forEach(ghost -> ghost.tick(game));
        game.level().optBonus().ifPresent(bonus -> bonus.tick(game));
        game.updateHunting();
        if (timer().hasExpired()) {
            if (game.level().number() == lastTestedLevelNumber) {
                game.publishGameEvent(GameEvent.Type.STOP_ALL_SOUNDS);
                game.control().changeState("INTRO");
            } else {
                timer().restartSeconds(TEST_DURATION_SEC);
                game.startNextLevel();
                configureLevelForTest(game);
            }
        }
        else if (game.isLevelCompleted()) {
            game.control().changeState("INTRO");
        } else if (game.hasPacManBeenKilled()) {
            timer.expire();
        } else if (game.hasGhostBeenKilled()) {
            game.control().changeState("EATING_GHOST");
        }
    }

    @Override
    public void onExit(Game game) {
        game.levelCounter().clear();
    }
}
