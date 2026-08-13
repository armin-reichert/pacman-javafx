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
        final GamePlay gamePlay = game.gamePlay();
        final GameEventManager eventManager = game.eventManager();
        final GameSession session = game.session();

        lastTestedLevelNumber = game.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : game.rules().lastLevelNumber();

        timer().restartSeconds(TEST_DURATION_SEC);

        gamePlay.buildNormalLevel(game, 1, game.gameVariantConfig().initialLifeCount());
        gamePlay.startLevel(game);
        configureLevelForTest(game);

        // Note: This event is very important because it triggers the creation of the actor animations!
        eventManager.publishGameEvent(new LevelStartedEvent(session.assertLevel()));
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameLevel level = game.session().assertLevel();
        final GameEventManager eventManager = game.eventManager();

        if (timer().hasExpired()) {
            if (level.number() == lastTestedLevelNumber) {
                // All levels tested, return to intro page
                eventManager.publishGameEvent(new StopAllSoundsEvent());
                game.session().gameFlow().enterState(game, CommonGameStateID.GAME_INTRO);
            }
            else {
                // Test next level
                game.gamePlay().startNextLevel(game);
                configureLevelForTest(game);
                timer().restartSeconds(TEST_DURATION_SEC);
            }
        }
        else {
            game.gamePlay().hunt(game, level);
            if (game.rules().isLevelCompleted(level)) {
                game.session().gameFlow().enterState(game, CommonGameStateID.GAME_INTRO);
            }
            else if (game.session().thisFrame().huntingStep().pacKilled()) {
                triggerTimeout();
            }
            else if (game.session().thisFrame().huntingStep().hasGhostBeenKilled()) {
                game.session().gameFlow().enterState(game, CommonGameStateID.GAME_LEVEL_EATING_GHOST);
            }
        }
    }

    @Override
    public void onExit(GameContext game) {
        LevelCounterSystem.clear(game.session().levelCounter());
    }

    private void configureLevelForTest(GameContext game) {
        final GameSystems sys = game.systems();
        final GameSession session = game.session();
        final GameLevel level = session.assertLevel();
        final Pac pac = level.entities().pac();

        pac.show();

        pac.cheats().usingAutopilotProperty().unbind();
        pac.cheats().setUsingAutopilot(true);

        sys.spriteAnim().playSelected(pac);

        final List<Ghost> ghosts = level.entities().ghosts();
        ghosts.forEach(ghost -> {
            ghost.show();
            sys.spriteAnim().playSelected(ghost);
        });

        session.hud().show();
        game.eventManager().publishGameEvent(new StopAllSoundsEvent());
    }
}
