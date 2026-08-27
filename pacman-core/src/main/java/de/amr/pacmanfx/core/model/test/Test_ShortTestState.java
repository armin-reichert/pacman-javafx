/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.TestStartedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gameplay.GamePlay;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;

public class Test_ShortTestState extends GameState {

    private int lastTestedLevelNumber;

    public Test_ShortTestState() {
        super(TestStateID.LEVEL_TEST_S);
    }

    @Override
    public void onEnter(GameContext game) {
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameSession session = game.session();

        //coinMechanism.setNumCoins(1);

        lastTestedLevelNumber = game.variant().rules().lastLevelNumber() == Integer.MAX_VALUE
            ? 25
            : game.variant().rules().lastLevelNumber();

        final GameLevel newLevel = gamePlay.buildNormalLevel(game, 1);
        game.eventManager().publishGameEvent(new LevelCreatedEvent(newLevel));


        final GameLevel level = session.level();
        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        timer().resetToIndefiniteDuration();

        gamePlay.startLevel(game, level);
    }

    @Override
    public void onUpdate(GameContext game) {
        final GameSystems systems = game.variant().systems();
        final GameSession session = game.session();
        final GamePlay gamePlay = game.variant().gamePlay();
        final GameLevel level = session.level();

        final float START = 1.0f;

        if (timer().atSecond(START)) {
            gamePlay.prepareLevelForPlaying(game, level);
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);
            gamePlay.showMessage(game, MessageType.READY);
            session.hud().hideCredit();
            session.hud().livesCounter().show();
            level.heartbeat().restart();
            game.eventManager().publishGameEvent(new TestStartedEvent(level));
        }
        else if (timer().atSecond(START + 1)) {
            session.hud().messageView().data().setMessageType(MessageType.NO_MESSAGE);
        }
        else if (timer().atSecond(START + 3)) {
            gamePlay.activateNextBonus(game, level);
        }
        else if (timer().atSecond(START + 5)) {
            level.entities().optBonus().ifPresent(bonus -> {
                systems.bonusState().showEatenForSeconds(bonus, 2);
                systems.worldNavigator().setMoveDirSpeed(bonus, 0);
                game.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer().atSecond(START + 6)) {
            gamePlay.activateNextBonus(game, level);
        }
        else if (timer().atSecond(START + 8)) {
            level.entities().optBonus().ifPresent(bonus -> {
                systems.bonusState().showEatenForSeconds(bonus, 2);
                systems.worldNavigator().setMoveDirSpeed(bonus, 0);
                game.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer().atSecond(START + 9)) {
            level.entities().pac().hide();
            level.entities().ghosts().forEach(GameEntity::hide);
            level.heartbeat().stop();
            finishLevel(level, systems);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                game.variant().gameFlow().restartGameState(game, CommonGameStateID.BOOT);
            } else {
                timer().resetToIndefiniteDuration();
                gamePlay.startNextLevel(game);
            }
        } else {
            level.entities().optBonus().ifPresent(bonus -> systems.entityUpdater().updateBonus(game, level, bonus));
        }
    }

    @Override
    public void onExit(GameContext game) {
        final LevelCounterSystem levelCounterSystem = game.variant().systems().levelCounterSystem();
        final LevelCounter levelCounter = game.session().hud().levelCounter();
        levelCounterSystem.clearCounter(levelCounter);
    }

    private void finishLevel(GameLevel level, GameSystems systems) {
        level.huntingTimerStrategy().stop();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().reset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.food().eatAll();

        // Pac-Man stops and stands still
        final Pac pac = level.entities().pac();
        systems.pacState().setState(pac, PacState.SLEEPING);
        systems.pacPower().reset(pac);

        // Ghosts stop
        level.entities().ghosts().forEach(ghost -> systems.worldNavigator().setDisabled(ghost, true));

        level.entities().optBonus().ifPresent(bonus -> {
            systems.bonusState().setBonusInactive(bonus);
            bonus.optComp(BonusMoveAndJumpComp.class).ifPresent(_-> systems.bonusMoveAndJump().setBonusInactive(bonus));
            level.entities().remove(bonus);
        });

        timer().resetToIndefiniteDuration();
    }
}