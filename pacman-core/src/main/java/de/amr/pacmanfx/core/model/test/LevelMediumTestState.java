/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventManager;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

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
    public void onEnter(GameContext gameContext) {
        final GamePlay gamePlay = gameContext.gamePlay();
        final GameModel model = gameContext.model();
        final GameEventManager eventManager = gameContext.eventManager();

        lastTestedLevelNumber = model.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : model.rules().lastLevelNumber();

        timer().restartSeconds(TEST_DURATION_SEC);

        gamePlay.resetForNewGame(gameContext);
        gamePlay.buildNormalLevel(gameContext, 1);
        gamePlay.startLevel(gameContext);
        configureLevelForTest(gameContext);

        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager.publishGameEvent(new LevelStartedEvent(model.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();
        final GameEventManager eventManager = gameContext.eventManager();

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                eventManager.publishGameEvent(new StopAllSoundsEvent());
                gameContext.flow().enterState(GameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                gameContext.gamePlay().startNextLevel(gameContext);
                configureLevelForTest(gameContext);
                timer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            gameContext.gamePlay().hunt(gameContext);
            if (model.rules().isLevelCompleted(level)) {
                gameContext.flow().enterState(GameStateID.GAME_INTRO);
            }
            else if (gameContext.thisFrame().huntingStepResult().pacKilled()) {
                triggerTimeout();
            }
            else if (gameContext.thisFrame().huntingStepResult().hasGhostBeenKilled()) {
                gameContext.flow().enterState(GameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        gameModel.levelCounter().clear();
    }

    private void configureLevelForTest(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        final Pac pac = level.entities().pac();
        pac.usingAutopilotProperty().unbind();
        pac.setUsingAutopilot(true);
        pac.animations().playSelected();
        pac.show();

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> ghost.animations().playSelected());
        ghosts.forEach(Ghost::show);

        gameContext.hudState().show();

        gameContext.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }
}
