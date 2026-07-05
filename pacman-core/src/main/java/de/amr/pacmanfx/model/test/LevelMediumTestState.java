/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.simulation.EntityCollisionDetector;

import java.util.List;

public class LevelMediumTestState extends GameState {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    public LevelMediumTestState() {
        super(TestStateID.LEVEL_TEST_M);
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

        gameModel.hudState().showIt();

        gameContext.flow().publishGameEvent(new StopAllSoundsEvent(gameContext));
    }

    @Override
    public String name() {
        return "LevelMediumTestState";
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        lastTestedLevelNumber = model.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : model.rules().lastLevelNumber();
        timer().restartSeconds(TEST_DURATION_SEC);
        context.gamePlay().resetForNewGame(context);
        context.gamePlay().buildNormalLevel(context, 1);
        context.gamePlay().startLevel(context, model.assertLevel());
        // Note: This event is very important because it triggers the creation of the actor animations!
        context.flow().publishGameEvent(new LevelStartedEvent(context, model.assertLevel()));
        configureLevelForTest(context);
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel gameModel = context.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();

        level.entities().pac().update(context, level);
        level.entities().ghosts().forEach(ghost -> ghost.update(context, level));
        level.optBonus().ifPresent(bonus -> bonus.update(context, level));

        if (gameModel.gateKeeper() != null) {
            gameModel.gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        context.cheats().update(level);

        level.heartbeat().triggerPulse();

        final EntityCollisionDetector collisionDetector = new EntityCollisionDetector(context);
        collisionDetector.detectCollisions(level);

        //TODO add missing logic again
        boolean pacKilled = false;

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                context.flow().publishGameEvent(new StopAllSoundsEvent(context));
                context.flow().enterState(GameStateID.GAME_INTRO);
            }
            else {
                timer().restartSeconds(TEST_DURATION_SEC);
                context.gamePlay().startNextLevel(context, gameModel.assertLevel());
                configureLevelForTest(context);
            }
        }
        else if (gameModel.rules().isLevelCompleted(level)) {
            context.flow().enterState(GameStateID.GAME_INTRO);
        }
        else if (pacKilled) {
            triggerTimeout();
        }
        else if (context.huntingStepResult().hasGhostBeenKilled()) {
            context.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel gameModel = context.model();
        gameModel.levelCounter().clear();
    }
}
