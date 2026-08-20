/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;

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
    public void onEnter(GameContext game) {
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameEventManager eventManager = game.eventManager();
        final GameSession session = game.session();

        lastTestedLevelNumber = game.variant().rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : game.variant().rules().lastLevelNumber();

        timer().restartSeconds(TEST_DURATION_SEC);

        final GameLevel newLevel = gamePlay.buildNormalLevel(game, 1, game.variant().initialLifeCount());
        game.eventManager().publishGameEvent(new LevelCreatedEvent(newLevel));

        gamePlay.startLevel(game);
        configureLevelForTest(game);

        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager.publishGameEvent(new LevelStartedEvent(session.level()));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameLevel level = game.session().level();
        final GameEventManager eventManager = game.eventManager();

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                eventManager.publishGameEvent(new StopAllSoundsEvent());
                game.variant().gameFlow().enterState(game, CommonGameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                game.variant().gamePlay().startNextLevel(game);
                configureLevelForTest(game);
                timer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            game.variant().gamePlay().updateGamePlay(game, level);
            if (game.variant().rules().isLevelCompleted(level)) {
                game.variant().gameFlow().enterState(game, CommonGameStateID.GAME_INTRO);
            }
            else if (game.session().thisFrame().gamePlayStep().pacKilled()) {
                triggerTimeout();
            }
            else if (game.session().thisFrame().gamePlayStep().hasGhostBeenKilled()) {
                game.variant().gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }
    }

    @Override
    public void onExit(GameContext game) {
        final LevelCounterSystem levelCounterSystem = game.variant().systems().levelCounterSystem();
        levelCounterSystem.clearCounter(game.session().levelCounter());
    }

    private void configureLevelForTest(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GameLevel level = session.level();
        final Pac pac = level.entities().pac();

        pac.show();

        pac.cheats().usingAutopilotProperty().unbind();
        pac.cheats().setUsingAutopilot(true);

        systems.spriteAnimController().playSelected(pac);

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> {
            ghost.show();
            systems.spriteAnimController().playSelected(ghost);
        });

        session.hud().show();
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }
}
