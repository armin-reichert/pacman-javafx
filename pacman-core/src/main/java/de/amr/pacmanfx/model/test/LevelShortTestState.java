/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.GameBox;
import de.amr.pacmanfx.event.BonusEatenEvent;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.*;

public class LevelShortTestState implements StateMachine.State<Game>, TestState {

    private final TickTimer timer = new TickTimer("Timer_" + name());
    private int lastTestedLevelNumber;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    @Override
    public void onEnter(Game game) {
        GameBox.instance().coinMechanism().setNumCoins(1);
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartIndefinitely();
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel(game.level());
        game.level().showPacAndGhosts();
    }

    @Override
    public void onUpdate(Game game) {
        final float START = 1.0f;
        if (timer.atSecond(START)) {
            game.continuePlaying(game.level(), 1);
            GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
            message.setPosition(game.level().worldMap().terrainLayer().messageCenterPosition());
            game.level().setMessage(message);
            game.level().blinking().restart();
        }
        else if (timer.atSecond(START + 1)) {
            game.clearLevelMessage();
        }
        else if (timer.atSecond(START + 3)) {
            game.activateNextBonus(game.level());
        }
        else if (timer.atSecond(START + 5)) {
            game.level().optBonus().ifPresent(bonus -> {
                bonus.setEatenSeconds(2);
                game.publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(game.level());
        }
        else if (timer.atSecond(START + 8)) {
            game.level().optBonus().ifPresent(bonus -> {
                bonus.setEatenSeconds(2);
                game.publishGameEvent(new BonusEatenEvent(bonus));
            });
        }
        else if (timer.atSecond(START + 9)) {
            game.level().hidePacAndGhosts();
            game.level().blinking().stop();
            game.onLevelCompleted(game.level());
        }
        else if (timer.atSecond(START + 10)) {
            if (game.level().number() == lastTestedLevelNumber) {
                GameBox.instance().coinMechanism().setNumCoins(0);
                game.boot();
                game.control().restartStateNamed(GameControl.StateName.BOOT.name());
            } else {
                timer.restartIndefinitely();
                game.startNextLevel();
                GameLevelMessage message = new GameLevelMessage(GameLevelMessageType.TEST);
                message.setPosition(game.level().worldMap().terrainLayer().messageCenterPosition());
                game.level().setMessage(message);
            }
        } else {
            game.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.tick(game));
        }
    }

    @Override
    public void onExit(Game game) {
        GameBox.instance().coinMechanism().setNumCoins(0);
        game.boot();
        game.clearLevelCounter();
    }
}