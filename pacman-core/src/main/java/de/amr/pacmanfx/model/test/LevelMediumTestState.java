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

    private void configureLevelForTest(GameContext gameContext) {
        final GameModel gameModel = gameContext.gameModel();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();

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

        gameModel.hud().show();

        gameContext.gameFlow().publishGameEvent(new StopAllSoundsEvent(gameContext));
    }

    @Override
    public String name() {
        return "LevelMediumTestState";
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel game = gameContext.gameModel();
        lastTestedLevelNumber = gameContext.gameRules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : gameContext.gameRules().lastLevelNumber();
        timer.restartSeconds(TEST_DURATION_SEC);
        game.prepareNewGame();
        game.buildNormalLevel(gameContext, 1);
        game.startLevel(gameContext);
        configureLevelForTest(gameContext);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.gameModel();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();

        level.entities().pac().update(gameContext, level);
        level.entities().ghosts().forEach(ghost -> ghost.update(gameContext, level));
        level.optBonus().ifPresent(bonus -> bonus.update(gameContext, level));

        if (gameModel.gateKeeper() != null) {
            gameModel.gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        gameModel.cheats().update(level);

        level.heartbeat().triggerPulse();

        gameContext.startNewHuntingStep();
        HuntingCollisionDetector.detectCollisions(gameContext);

        //TODO add missing logic again
        boolean pacKilled = false;

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                gameContext.gameFlow().publishGameEvent(new StopAllSoundsEvent(gameContext));
                gameContext.gameFlow().enterState(GameStateID.GAME_INTRO.name());
            }
            else {
                timer().restartSeconds(TEST_DURATION_SEC);
                gameModel.startNextLevel(gameContext);
                configureLevelForTest(gameContext);
            }
        }
        else if (gameContext.gameRules().isLevelCompleted(level)) {
            gameContext.gameFlow().enterState(GameStateID.GAME_INTRO.name());
        }
        else if (pacKilled) {
            expire();
        }
        else if (gameContext.huntingResult().hasGhostBeenKilled()) {
            gameContext.gameFlow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST.name());
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel game = context.gameModel();
        game.levelCounter().clear();
    }
}
