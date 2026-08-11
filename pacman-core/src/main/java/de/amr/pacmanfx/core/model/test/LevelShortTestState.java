/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.event.TestStartedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelStartedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelMessageType;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.session.GameSession;

public class LevelShortTestState extends GameState {

    private int lastTestedLevelNumber;

    public LevelShortTestState() {
        super(TestStateID.LEVEL_TEST_S);
    }

    @Override
    public void onEnter(GameContext game) {
        final GamePlay gamePlay = game.gamePlay();
        final GameModel model = game.model();
        final GameSession session = game.session();

        //coinMechanism.setNumCoins(1);

        lastTestedLevelNumber = model.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : model.rules().lastLevelNumber();

        gamePlay.buildNormalLevel(game, 1, model.initialLifeCount());
        gamePlay.startLevel(game);

        final GameLevel level = session.assertLevel();
        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        waitForTimeout();
        // Note: This event is very important because it triggers the creation of the actor animations!
        game.eventManager().publishGameEvent(new LevelStartedEvent(level));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();
        final GameSession session = gameContext.session();
        final GameLevel level = session.assertLevel();

        final float START = 1.0f;

        if (timer().atSecond(START)) {
            gameContext.gamePlay().prepareLevelForPlaying(gameContext);
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
            gameContext.gamePlay().showLevelMessage(level, GameLevelMessageType.READY);
            session.hud().hideCredit().showLivesCounter();

            level.heartbeat().restart();

            gameContext.eventManager().publishGameEvent(new TestStartedEvent(level));
        }
        else if (timer().atSecond(START + 1)) {
            level.clearMessage();
        }
        else if (timer().atSecond(START + 3)) {
            gameContext.gamePlay().activateNextBonus(gameContext, level);
        }
        else if (timer().atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                sys.bonusState().showEatenForSeconds(bonus, 2);
                gameContext.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer().atSecond(START + 6)) {
            gameContext.gamePlay().activateNextBonus(gameContext, level);
        }
        else if (timer().atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                sys.bonusState().showEatenForSeconds(bonus, 2);
                gameContext.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer().atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.heartbeat().stop();
            gameContext.gamePlay().onLevelCompleted(gameContext, level);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                gameContext.flow().restartState(gameContext, CommonGameStateID.BOOT);
            } else {
                waitForTimeout();
                gameContext.gamePlay().startNextLevel(gameContext);
            }
        } else {
            sys.bonusState().update(gameContext);
        }
    }

    @Override
    public void onExit(GameContext game) {
        LevelCounterSystem.clear(game.session().levelCounter());
    }
}