/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.*;

public class LevelShortTestState implements StateMachine.State<Game>, TestState {

    private final TickTimer timer = new TickTimer("Timer_" + name());
    private int lastTestedLevelNumber;

    private static final CoinMechanism COIN_MECHANISM = Globals.THE_GAME_BOX;

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
        COIN_MECHANISM.setNumCoins(1);
        lastTestedLevelNumber = game.lastLevelNumber() == Integer.MAX_VALUE ? 25 : game.lastLevelNumber();
        timer.restartIndefinitely();
        game.prepareNewGame();
        game.buildNormalLevel(1);
        game.startLevel();
        game.level().showPacAndGhosts();
    }

    @Override
    public void onUpdate(Game game) {
        final float START = 1.0f;
        if (timer.atSecond(START)) {
            game.continuePlaying(1);
            GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
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
            game.level().optBonus().ifPresent(bonus -> bonus.setEatenSeconds(2));
            game.publishGameEvent(GameEvent.Type.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 6)) {
            game.activateNextBonus(game.level());
        }
        else if (timer.atSecond(START + 8)) {
            game.level().optBonus().ifPresent(bonus -> bonus.setEatenSeconds(2));
            game.publishGameEvent(GameEvent.Type.BONUS_EATEN);
        }
        else if (timer.atSecond(START + 9)) {
            game.level().hidePacAndGhosts();
            game.level().blinking().stop();
            game.onLevelCompleted();
        }
        else if (timer.atSecond(START + 10)) {
            if (game.level().number() == lastTestedLevelNumber) {
                COIN_MECHANISM.setNumCoins(0);
                game.boot();
                game.control().restart(GameControl.StateName.BOOT.name());
            } else {
                timer.restartIndefinitely();
                game.startNextLevel();
                GameLevelMessage message = new GameLevelMessage(MessageType.TEST);
                message.setPosition(game.level().worldMap().terrainLayer().messageCenterPosition());
                game.level().setMessage(message);
            }
        } else {
            game.optGameLevel().flatMap(GameLevel::optBonus).ifPresent(bonus -> bonus.tick(game));
        }
    }

    @Override
    public void onExit(Game game) {
        COIN_MECHANISM.setNumCoins(0);
        game.boot();
        game.levelCounter().clearLevelCounter();
    }
}