/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;

public class LevelMediumTestState<GAME extends Game> extends TestState<GAME> {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    private void configureLevelForTest(GAME game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        level.pac().usingAutopilotProperty().unbind();
        level.pac().setUsingAutopilot(true);
        level.pac().playAnimation();
        level.ghosts().forEach(Actor::playAnimation);
        level.pac().show();
        level.ghosts().forEach(Ghost::show);
        GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);
        game.hud().show();
        game.flow().publishGameEvent(new StopAllSoundsEvent(game));
    }

    @Override
    public void onEnter(GAME game) {
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        configureLevelForTest(game);
    }

    @Override
    public void onUpdate(GAME game) {
        final GameLevel level = game.optGameLevel().orElseThrow();
        level.pac().tick(game);
        level.ghosts().forEach(ghost -> ghost.tick(game));
        level.optBonus().ifPresent(bonus -> bonus.tick(game));
        game.doLevelPlaying();
        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                game.flow().publishGameEvent(new StopAllSoundsEvent(game));
                game.flow().enterStateWithName(GameFlow.CanonicalGameState.INTRO.name());
            } else {
                timer().restartSeconds(TEST_DURATION_SEC);
                game.startNextLevel();
                configureLevelForTest(game);
            }
        }
        else if (game.isLevelCompleted()) {
            game.flow().enterStateWithName(GameFlow.CanonicalGameState.INTRO.name());
        } else if (game.hasPacManBeenKilled()) {
            expire();
        } else if (game.hasGhostBeenKilled()) {
            game.flow().enterStateWithName(GameFlow.CanonicalGameState.EATING_GHOST.name());
        }
    }

    @Override
    public void onExit(GAME game) {
        game.levelCounter().clear();
    }
}
