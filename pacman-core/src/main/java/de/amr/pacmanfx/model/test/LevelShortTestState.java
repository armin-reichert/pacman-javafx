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
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        //coinMechanism.setNumCoins(1);
        lastTestedLevelNumber = model.rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : model.rules().lastLevelNumber();
        context.gamePlay().resetForNewGame(context);
        context.gamePlay().buildNormalLevel(context, 1);
        context.gamePlay().startLevel(context, model.assertLevel());
        final GameLevel level = model.optGameLevel().orElseThrow();
        level.entities().pac().show();
        level.entities().ghosts().forEach(Ghost::show);

        waitForTimeout();
        // Note: This event is very important because it triggers the creation of the actor animations!
        context.flow().publishGameEvent(new LevelStartedEvent(context, level));
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel model = context.model();
        final GameLevel level = model.optGameLevel().orElseThrow();
        final float START = 1.0f;
        if (timer().atSecond(START)) {
            context.gamePlay().prepareLevelForPlaying(level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(Ghost::show);
            context.gamePlay().showLevelMessage(level, GameLevelMessageType.READY);
            model.hudState().creditOff().livesCounterOn();

            level.heartbeat().restart();

            context.flow().publishGameEvent(new TestStartedEvent(context, level));
        }
        else if (timer().atSecond(START + 1)) {
            level.clearMessage();
        }
        else if (timer().atSecond(START + 3)) {
            context.gamePlay().activateNextBonus(context, level);
        }
        else if (timer().atSecond(START + 5)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                context.flow().publishGameEvent(new BonusEatenEvent(context, bonus));
            });
        }
        else if (timer().atSecond(START + 6)) {
            context.gamePlay().activateNextBonus(context, level);
        }
        else if (timer().atSecond(START + 8)) {
            level.optBonus().ifPresent(bonus -> {
                bonus.showEatenForSeconds(2);
                context.flow().publishGameEvent(new BonusEatenEvent(context, bonus));
            });
        }
        else if (timer().atSecond(START + 9)) {
            level.hidePacAndGhosts();
            level.heartbeat().stop();
            context.gamePlay().onLevelCompleted(level);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                context.flow().restartState(GameStateID.BOOT);
            } else {
                waitForTimeout();
                context.gamePlay().startNextLevel(context, level);
            }
        } else {
            model.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.update(context, level));
        }
    }

    @Override
    public void onExit(GameContext context) {
        final GameModel gameModel = context.model();
        context.gamePlay().init(context);
        gameModel.levelCounter().clear();
    }
}