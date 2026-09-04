/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.basics.timer.Pulse;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.BonusPoints;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusMoveAndJumpComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacState;
import de.amr.pacmanfx.core.event.TestStartedEvent;
import de.amr.pacmanfx.core.event.bonus.BonusEatenEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.MessageType;
import org.tinylog.Logger;

public class Test_ShortTestState extends AbstractGameState {

    private int lastTestedLevelNumber;

    public Test_ShortTestState() {
        super(TestStateID.LEVEL_TEST_S);
    }

    @Override
    public void onEnterState(GameContext game) {
        lastTestedLevelNumber = rules.lastLevelNumber() == Integer.MAX_VALUE ? 25 : rules.lastLevelNumber();

        final GameLevel level = gamePlay.buildNormalLevel(game, 1);
        game.eventManager().publishGameEvent(new LevelCreatedEvent(level));

        level.entities().pac().show();
        level.entities().ghosts().forEach(GameEntity::show);

        timer().resetToIndefiniteDuration();

        gamePlay.startLevel(game, level);
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        final GameLevel level = session.level();
        final float START = 1.0f;

        if (timer().atSecond(START)) {
            hud.creditDisplay().hide();
            hud.livesCounter().show();

            level.heartbeat().restart();
            level.entities().pac().show();
            level.entities().ghosts().forEach(GameEntity::show);

            gamePlay.prepareLevelForPlaying(game, level);
            gamePlay.showMessage(game, MessageType.READY);

            game.eventManager().publishGameEvent(new TestStartedEvent(level));
        }
        else if (timer().atSecond(START + 1)) {
            hud.clearMessage();
        }
        else if (timer().atSecond(START + 3)) {
            gamePlay.activateNextBonus(game, level);
        }
        else if (timer().atSecond(START + 5)) {
            level.entities().optBonus().ifPresent(bonus -> eatBonus(game, level, bonus));
        }
        else if (timer().atSecond(START + 6)) {
            gamePlay.activateNextBonus(game, level);
        }
        else if (timer().atSecond(START + 8)) {
            level.entities().optBonus().ifPresent(bonus -> eatBonus(game, level, bonus));
        }
        else if (timer().atSecond(START + 9)) {
            level.entities().pac().hide();
            level.entities().ghosts().forEach(GameEntity::hide);
            level.heartbeat().stop();
            finishLevel(level, systems);
        }
        else if (timer().atSecond(START + 10)) {
            if (level.number() == lastTestedLevelNumber) {
                flow.restartGameState(game, CommonGameStateID.BOOT);
            } else {
                timer().resetToIndefiniteDuration();
                gamePlay.startNextLevel(game);
            }
        }
    }

    private void eatBonus(GameContext game, GameLevel level, Bonus bonus) {
        // Bonus value depends on game variant and bonus type
        final int bonusValue = rules.scoringRules().pointsForBonus(bonus.data().symbolCode());
        gamePlay.scorePoints(game, bonusValue, level.number());
        Logger.info("Scored {} points for eating bonus {}", bonusValue, bonus);

        level.entities().remove(bonus);

        // Eaten bonus is displayed as points for short time
        final var points = new BonusPoints(bonusValue);
        points.pos().set(bonus.pos().asVector2f());
        points.setLifetimeSec(rules.eatenBonusDisplaySeconds());
        points.show();
        level.entities().add(points);

        game.eventManager().publishGameEvent(new BonusEatenEvent(bonus));
    }

    @Override
    public void onExit(GameContext game) {
        systems.levelCounterSystem().clear(hud.levelCounter());
    }

    private void finishLevel(GameLevel level, GameSystems systems) {
        level.huntingTimer().stop();

        level.heartbeat().setStartState(Pulse.State.OFF);
        level.heartbeat().stopAndReset();

        // If level was ended by cheat, there might still be food remaining, so eat it:
        level.food().eatAll();

        // Pac-Man stops and stands still
        final Pac pac = level.entities().pac();
        pac.state().setEnumValue(PacState.SLEEPING);
        systems.pacPower().stopAndReset(pac);

        // Ghosts stop
        level.entities().ghosts().forEach(ghost -> ghost.worldNavigation().setPaused(true));

        level.entities().optBonus().ifPresent(bonus -> {
            systems.bonusState().setInactive(bonus);
            bonus.optComp(BonusMoveAndJumpComp.class).ifPresent(_-> systems.bonusMoveAndJump().setBonusInactive(bonus));
            level.entities().remove(bonus);
        });
        level.clearBonusIndex();

        timer().resetToIndefiniteDuration();
    }
}