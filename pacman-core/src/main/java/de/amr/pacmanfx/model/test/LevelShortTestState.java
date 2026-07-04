/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.event.LevelStartedEvent;
import de.amr.pacmanfx.event.TestStartedEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessageType;

public class LevelShortTestState extends GameState {

    private int lastTestedLevelNumber;

    public LevelShortTestState() {
        super(TestStateID.LEVEL_TEST_S);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        //coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = gameModel.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : gameModel.rules().lastLevelNumber();
        gameModel.resetForNewGame();
        gameModel.buildNormalLevel(gameContext, 1);
        gameModel.startLevel(gameContext, gameContext.assertLevel());
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        waitForTimeout();
        // Note: This event is very important because it triggers the creation of the actor animations!
        gameContext.flow().publishGameEvent(new LevelStartedEvent(gameContext, level));
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameLevel level = gameModel.optGameLevel().orElseThrow();
        final float START = 1.0f;
        if (timer().atSecond(START)) {
            gameModel.prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
            gameModel.showLevelMessage(level, GameLevelMessageType.READY);
            gameModel.hudState().creditOff().livesCounterOn();

            level.heartbeat().restart();

            gameContext.flow().publishGameEvent(new TestStartedEvent(gameContext, level));
        }
        else if (timer().atSecond(START + 1)) {
            level.clearMessage();
        }
        else if (timer().atSecond(START + 3)) {
            gameModel.activateNextBonus(gameContext, level);
        }
        else if (timer().atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                gameContext.flow().publishGameEvent(new BonusEatenEvent(gameContext, bonus));
            });
        }
        else if (timer().atSecond(START + 6)) {
            gameModel.activateNextBonus(gameContext, level);
        }
        else if (timer().atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                gameContext.flow().publishGameEvent(new BonusEatenEvent(gameContext, bonus));
            });
        }
        else if (timer().atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.heartbeat().stop();
            gameModel.onLevelCompleted(level);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                gameContext.flow().restartState(GameStateID.BOOT);
            } else {
                waitForTimeout();
                gameModel.startNextLevel(gameContext, level);
            }
        } else {
            gameModel.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.update(gameContext, level));
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel gameModel = context.model();
        gameModel.init();
        gameModel.levelCounter().clear();
    }
}