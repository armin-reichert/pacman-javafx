/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.simulation.EntityCollisionDetector;

import java.util.List;

public class LevelMediumTestState extends GameState implements TestState {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    public LevelMediumTestState() {
        super("Medium Level Test State");
    }

    private void configureLevelForTest(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.usingAutopilotProperty().unbind();
        pac.setUsingAutopilot(true);
        pac.animations().playSelected();
        pac.show();

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> ghost.animations().playSelected());
        ghosts.forEach(Ghost::show);

        gameModel.hud().show();

        gameContext.flow().publishGameEvent(new StopAllSoundsEvent(gameContext));
    }

    @Override
    public String name() {
        return "LevelMediumTestState";
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        lastTestedLevelNumber = gameContext.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : gameContext.rules().lastLevelNumber();
        timer().restartSeconds(TEST_DURATION_SEC);
        gameModel.resetForNewGame();
        gameModel.buildNormalLevel(gameContext, 1);
        gameModel.startLevel(gameContext);
        configureLevelForTest(gameContext);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();

        level.entities().pac().update(gameContext, level);
        level.entities().ghosts().forEach(ghost -> ghost.update(gameContext, level));
        level.optBonus().ifPresent(bonus -> bonus.update(gameContext, level));

        if (gameModel.gateKeeper() != null) {
            gameModel.gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        gameContext.cheats().update(level);

        level.heartbeat().triggerPulse();

        final EntityCollisionDetector collisionDetector = new EntityCollisionDetector(gameContext);
        collisionDetector.detectCollisions(level);

        //TODO add missing logic again
        boolean pacKilled = false;

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                gameContext.flow().publishGameEvent(new StopAllSoundsEvent(gameContext));
                gameContext.flow().enterState(GameStateID.GAME_INTRO);
            }
            else {
                timer().restartSeconds(TEST_DURATION_SEC);
                gameModel.startNextLevel(gameContext);
                configureLevelForTest(gameContext);
            }
        }
        else if (gameContext.rules().isLevelCompleted(level)) {
            gameContext.flow().enterState(GameStateID.GAME_INTRO);
        }
        else if (pacKilled) {
            expire();
        }
        else if (gameContext.huntingStepResult().hasGhostBeenKilled()) {
            gameContext.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel gameModel = context.model();
        gameModel.levelCounter().clear();
    }
}
