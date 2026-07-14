/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.simulation.GamePlay;
import de.amr.pacmanfx.core.simulation.HuntingStepResult;

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
        final GamePlay gamePlay = context.gamePlay();
        final GameModel model = context.model();
        final GameEventManager eventManager = context.eventManager();

        lastTestedLevelNumber = model.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : model.rules().lastLevelNumber();

        timer().restartSeconds(TEST_DURATION_SEC);

        gamePlay.resetForNewGame(model);
        gamePlay.buildNormalLevel(context.createPlayContextWithoutLevel(), 1);
        gamePlay.startLevel(context.createPlayContext());
        configureLevelForTest(context);

        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager.publishGameEvent(new LevelStartedEvent(model.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.assertLevel();
        final GameEventManager eventManager = context.eventManager();

        model.clearHuntingStepResult();

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                eventManager.publishGameEvent(new StopAllSoundsEvent());
                context.flow().enterState(GameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                context.gamePlay().startNextLevel(context.createPlayContext());
                configureLevelForTest(context);
                timer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            final HuntingStepResult result = context.gamePlay().hunt(context.createPlayContext());
            model.setHuntingStepResult(result);

            if (model.rules().isLevelCompleted(level)) {
                context.flow().enterState(GameStateID.GAME_INTRO);
            }
            else if (result.pacKilled()) {
                triggerTimeout();
            }
            else if (result.hasGhostBeenKilled()) {
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

        model.hudState().show();

        context.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }
}
