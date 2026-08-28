/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;

import java.util.List;

public class Test_MediumTestState extends AbstractGameState {

    static final int TEST_DURATION_SEC = 10;

    private int lastTestedLevelNumber;

    public Test_MediumTestState() {
        super(TestStateID.LEVEL_TEST_M);
    }

    @Override
    public String name() {
        return "LevelMediumTestState";
    }

    @Override
    public void onEnterState(GameContext game) {
        lastTestedLevelNumber = rules.lastLevelNumber() == Integer.MAX_VALUE ? 25 : rules.lastLevelNumber();

        timer().restartSeconds(TEST_DURATION_SEC);

        final GameLevel level = gamePlay.buildNormalLevel(game, 1);
        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

        configureLevelForTest(game);
        gamePlay.startLevel(game, session.level());
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameLevel level = session.level();

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                game.eventManager().publishGameEvent(new StopAllSoundsEvent());
                flow.enterGameState(game, CommonGameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                gamePlay.startNextLevel(game);
                configureLevelForTest(game);
                timer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            gamePlay.update(game, level);
            if (rules.isLevelCompleted(level)) {
                flow.enterGameState(game, CommonGameStateID.GAME_INTRO);
            }
            else if (session.thisFrame().gamePlayStep().pacKilled()) {
                triggerTimeout();
            }
            else if (session.thisFrame().gamePlayStep().hasGhostBeenKilled()) {
                flow.enterGameState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }
    }

    @Override
    public void onExit(GameContext game) {
        systems.levelCounterSystem().clear(hud.levelCounter());
    }

    private void configureLevelForTest(GameContext game) {
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();

        hud.show();

        pac.show();
        systems.actorSpriteAnimController().playSelected(pac);

        pac.cheats().usingAutopilotProperty().unbind();
        pac.cheats().setUsingAutopilot(true);

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> {
            ghost.show();
            systems.actorSpriteAnimController().playSelected(ghost);
        });

        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }
}
