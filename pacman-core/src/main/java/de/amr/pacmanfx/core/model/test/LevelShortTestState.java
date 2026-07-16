/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.BonusEatenEvent;
import de.amr.pacmanfx.core.event.LevelStartedEvent;
import de.amr.pacmanfx.core.event.TestStartedEvent;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class LevelShortTestState extends GameState {

    private int lastTestedLevelNumber;

    public LevelShortTestState() {
        super(TestStateID.LEVEL_TEST_S);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        //coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = model.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : model.rules().lastLevelNumber();
        gameContext.gamePlay().resetForNewGame(gameContext);
        gameContext.gamePlay().buildNormalLevel(gameContext, 1);
        gameContext.gamePlay().startLevel(gameContext);
        final GameLevel level = model.optLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        waitForTimeout();
        // Note: This event is very important because it triggers the creation of the actor animations!
        gameContext.eventManager().publishGameEvent(new LevelStartedEvent(level));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = model.optLevel().orElseThrow();
        final float START = 1.0f;
        if (timer().atSecond(START)) {
            gameContext.gamePlay().prepareLevelForPlaying(gameContext);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
            gameContext.gamePlay().showLevelMessage(level, GameLevelMessageType.READY);
            model.hudState().hideCredit().showLivesCounter();

            level.heartbeat().restart();

            gameContext.eventManager().publishGameEvent(new TestStartedEvent(level));
        }
        else if (timer().atSecond(START + 1)) {
            level.clearMessage();
        }
        else if (timer().atSecond(START + 3)) {
            gameContext.gamePlay().activateNextBonus(gameContext);
        }
        else if (timer().atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                gameContext.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer().atSecond(START + 6)) {
            gameContext.gamePlay().activateNextBonus(gameContext);
        }
        else if (timer().atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                gameContext.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer().atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.heartbeat().stop();
            gameContext.gamePlay().onLevelCompleted(level);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                gameContext.flow().restartState(gameContext, GameStateID.BOOT);
            } else {
                waitForTimeout();
                gameContext.gamePlay().startNextLevel(gameContext);
            }
        } else {
            model.optLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.update(level, gameContext.eventManager()));
        }
    }

    @Override
    public void onExit(GameContext gameContext) {
        final GameModel model = gameContext.model();
        gameContext.gamePlay().init(gameContext);
        model.levelCounter().clear();
    }
}