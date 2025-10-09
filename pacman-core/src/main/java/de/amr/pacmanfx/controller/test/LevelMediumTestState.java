/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameLevelMessage;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.AnimationManager;

public class LevelMediumTestState implements TestGameState {
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
        gameLevel.pac().optAnimationManager().ifPresent(AnimationManager::play);
        gameLevel.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        gameLevel.showPacAndGhosts();
        GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
        message.setPosition(gameLevel.worldMap().terrainLayer().messageCenterPosition());
        gameLevel.setMessage(message);
        context.game().hud().creditVisible(false);
        context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public void onEnter(GameContext context) {
        lastTestedLevelNumber = context.game().lastLevelNumber() == Integer.MAX_VALUE ? 25 : context.game().lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        context.game().prepareForNewGame();
        context.game().buildNormalLevel(1);
        context.game().startLevel(context.gameLevel());
        configureLevelForTest(context);
    }

    @Override
    public void onUpdate(GameContext context) {
        final Game game = context.game();
        final GameLevel gameLevel = context.gameLevel();
        gameLevel.pac().tick(context);
        gameLevel.ghosts().forEach(ghost -> ghost.tick(context));
        gameLevel.bonus().ifPresent(bonus -> bonus.tick(context));
        game.updateHunting(gameLevel);
        if (timer().hasExpired()) {
            if (gameLevel.number() == lastTestedLevelNumber) {
                context.eventManager().publishEvent(GameEventType.STOP_ALL_SOUNDS);
                context.gameController().changeGameState(GamePlayState.INTRO);
            } else {
                timer().restartSeconds(TEST_DURATION_SEC);
                game.startNextLevel();
                configureLevelForTest(context);
            }
        }
        else if (game.isLevelCompleted(gameLevel)) {
            context.gameController().changeGameState(GamePlayState.INTRO);
        } else if (game.hasPacManBeenKilled()) {
            timer.expire();
        } else if (game.haveGhostsBeenKilled()) {
            context.gameController().changeGameState(GamePlayState.GHOST_DYING);
        }
    }

    @Override
    public void onExit(GameContext context) {
        context.game().levelCounter().clear();
    }
}
