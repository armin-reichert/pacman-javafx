/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.lib.fsm.AbstractState;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.AnimationManager;

public class LevelMediumTestState extends AbstractState<Game> implements TestState {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    private void configureLevelForTest(Game game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        level.pac().usingAutopilotProperty().unbind();
        level.pac().setUsingAutopilot(true);
        level.pac().optAnimationManager().ifPresent(AnimationManager::play);
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(AnimationManager::play));
        level.showPacAndGhosts();
        GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
        game.hud().show();
        game.publishGameEvent(new StopAllSoundsEvent());
    }

    @Override
    public void onEnter(Game game) {
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        game.prepareNewGame();
        game.buildNormalLevel(1);

        final GameLevel level = game.optGameLevel().orElseThrow();
        game.startLevel(level);
        configureLevelForTest(game);
    }

    @Override
    public void onUpdate(Game game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        level.pac().tick(game);
        level.ghosts().forEach(ghost -> ghost.tick(game));
        level.optBonus().ifPresent(bonus -> bonus.tick(game));
        game.whileHunting(level);
        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                game.publishGameEvent(new StopAllSoundsEvent());
                game.control().enterStateWithName(GameControl.CommonGameState.INTRO.name());
            } else {
                timer().restartSeconds(TEST_DURATION_SEC);
                game.startNextLevel();
                configureLevelForTest(game);
            }
        }
        else if (game.isLevelCompleted(level)) {
            game.control().enterStateWithName(GameControl.CommonGameState.INTRO.name());
        } else if (game.hasPacManBeenKilled()) {
            expire();
        } else if (game.hasGhostBeenKilled()) {
            game.control().enterStateWithName(GameControl.CommonGameState.EATING_GHOST.name());
        }
    }

    @Override
    public void onExit(Game game) {
        game.levelCounter().clear();
    }
}
