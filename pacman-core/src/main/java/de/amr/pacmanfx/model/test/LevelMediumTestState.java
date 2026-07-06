/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GameEventManager;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;

import java.util.List;

public class LevelMediumTestState extends GameState {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    public LevelMediumTestState() {
        super(TestStateID.LEVEL_TEST_M);
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
        context.gamePlay().resetForNewGame(model);
        context.gamePlay().buildNormalLevel(context.eventManager(), model, 1);
        context.gamePlay().startLevel(context.eventManager(), model.assertLevel());
        configureLevelForTest(context);
        // Note: This event is very important because it triggers the creation of the actor animations!
        context.eventManager().publishGameEvent(new LevelStartedEvent(model.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();
        final GameEventManager eventManager = context.eventManager();

        model.setHuntingStepResult(null);

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                eventManager.publishGameEvent(new StopAllSoundsEvent());
                context.flow().enterState(GameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                context.gamePlay().startNextLevel(eventManager, level);
                configureLevelForTest(context);
                timer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            model.setHuntingStepResult(context.gamePlay().hunt(eventManager, level));
            if (model.rules().isLevelCompleted(level)) {
                context.flow().enterState(GameStateID.GAME_INTRO);
            }
            else if (model.huntingStepResult().pacKilled()) {
                triggerTimeout();
            }
            else if (model.huntingStepResult().hasGhostBeenKilled()) {
                context.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel gameModel = context.model();
        gameModel.levelCounter().clear();
    }

    private void configureLevelForTest(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.optLevel().orElseThrow();

        final Pac pac = level.entities().pac();
        pac.usingAutopilotProperty().unbind();
        pac.setUsingAutopilot(true);
        pac.animations().playSelected();
        pac.show();

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> ghost.animations().playSelected());
        ghosts.forEach(Ghost::show);

        model.hudState().showIt();

        context.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }
}
