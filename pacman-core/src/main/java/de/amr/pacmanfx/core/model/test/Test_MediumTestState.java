/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.SimulationFrame;
import de.amr.pacmanfx.core.level.GameLevel;

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
    public void doEnterState(GameContext game) {
        lastTestedLevelNumber = game.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25 : game.rules().lastLevelNumber();

        stateTimer().restartSeconds(TEST_DURATION_SEC);

        final GameLevel level = game.play().buildNormalLevel(game, 1);
        game.eventManager().publishEvent(new LevelCreatedEvent(game, level));

        configureLevelForTest(game);
        game.play().startLevel(game, game.session().optLevel().orElseThrow());
    }

    @Override
    public void doUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        final GameLevel level = game.session().optLevel().orElseThrow();

        if (stateTimer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                game.eventManager().publishEvent(new StopAllSoundsEvent());
                game.flow().enterGameState(game, CommonGameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                game.play().startNextLevel(game);
                configureLevelForTest(game);
                stateTimer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            final SimulationFrame thisFrame = game.session().thisFrame();

            game.play().updateLevel(game, level);
            if (game.rules().isLevelCompleted(level)) {
                game.flow().enterGameState(game, CommonGameStateID.GAME_INTRO);
            }
            else if (thisFrame.pacKilled()) {
                triggerTimeout();
            }
            else if (thisFrame.hasGhostBeenKilled()) {
                game.flow().enterGameState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }
    }

    @Override
    public void onExit(GameContext game) {
        game.systems().levelCounterSystem().clear(game.session().hud().levelCounter());
    }

    private void configureLevelForTest(GameContext game) {
        final GameLevel level = game.session().optLevel().orElseThrow();
        final Pac pac = level.entitySet().pac();

        game.session().store(SessionKey.HUD_VISIBLE, true);

        pac.show();
        game.systems().actorSpriteAnimController().playSelected(pac);

        pac.cheats().usingAutopilotProperty().unbind();
        pac.cheats().setUsingAutopilot(true);

        level.entitySet().ghosts().forEach(ghost -> {
            ghost.show();
            game.systems().actorSpriteAnimController().playSelected(ghost);
        });

        game.eventManager().publishEvent(new StopAllSoundsEvent());
    }
}
