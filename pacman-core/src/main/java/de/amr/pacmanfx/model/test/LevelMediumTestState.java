/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.simulation.HuntingCollisionDetector;

import java.util.List;

public class LevelMediumTestState extends TestState {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    private void configureLevelForTest(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.usingAutopilotProperty().unbind();
        pac.setUsingAutopilot(true);
        pac.animations().playSelected();
        pac.show();

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> ghost.animations().playSelected());
        ghosts.forEach(Ghost::show);

        final var message = new GameLevelMessage(GameLevelMessageType.TEST);
        message.setPosition(level.worldMap().terrainLayer().messageCenterPosition());
        level.setMessage(message);

        game.hud().show();

        game.flow().publishGameEvent(new StopAllSoundsEvent(context));
    }

    @Override
    public String name() {
        return "LevelMediumTestState";
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel game = context.gameModel();
        lastTestedLevelNumber = game.rules().lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.rules().lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        configureLevelForTest(context);
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();

        level.entities().pac().update(level);
        level.entities().ghosts().forEach(ghost -> ghost.update(level));
        level.optBonus().ifPresent(bonus -> bonus.update(level));

        if (game.gateKeeper() != null) {
            game.gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        game.cheats().update(level);

        level.heartbeat().triggerPulse();

        context.startNewHuntingStep();
        HuntingCollisionDetector.detectCollisions(context);

        //TODO add missing logic again
        boolean pacKilled = false;

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                game.flow().publishGameEvent(new StopAllSoundsEvent(context));
                game.flow().enterState(GameStateID.GAME_INTRO.name());
            }
            else {
                timer().restartSeconds(TEST_DURATION_SEC);
                game.startNextLevel();
                configureLevelForTest(context);
            }
        }
        else if (game.rules().isLevelCompleted(level)) {
            game.flow().enterState(GameStateID.GAME_INTRO.name());
        }
        else if (pacKilled) {
            expire();
        }
        else if (context.huntingResult().hasGhostBeenKilled()) {
            game.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST.name());
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel game = context.gameModel();
        game.levelCounter().clear();
    }
}
